import os
import jwt
import uuid
import hashlib
from datetime import datetime
from flask import Blueprint, request, jsonify
from functools import wraps
from api.models.user import User
from api.models.token import Token
from . import fcm

bp = Blueprint("auth", __name__, url_prefix="/api/auth")

def only(*scopes):
	def onlyAdmin(func):
		@wraps(func)
		def func_wrapper(*args, **kwargs):
			access_token = request.headers.get("Authorization")
			if access_token is None:
				return jsonify(message="Access Token is missing"), 401

			token_data = decode_access_token(access_token)
			if token_data is None:
				return jsonify(message="Invalid token"), 401

			dbToken = Token.get(token_data['jti'])
			if dbToken is None:
				return jsonify(message="Invalid access token"), 401

			if 'member' in scopes:
				return func(*args, **kwargs)

			if 'admin' in scopes:
				user = User.get(token_data['username'])
				if user.isAdmin:
					return func(*args, **kwargs)

			if 'me' in scopes:
				if "username" not in kwargs:
					return jsonify(message="No permission"), 403

				if kwargs['username'] == token_data['username']:
					return func(*args, **kwargs)

			return jsonify(message="No permission"), 403
		return func_wrapper
	return onlyAdmin

@bp.route("/login", methods=["POST"], strict_slashes=False)
def login():
	if "username" not in request.json or "password" not in request.json:
		return jsonify(message="Username or password is missing"), 400

	username = request.json["username"]
	password = request.json["password"]

	user = User.get(username)
	passwordHashed = hashlib.sha256(password.encode('utf-8')).hexdigest()

	if user is None or passwordHashed != user.password or not user.isActivated:
		return jsonify(message="User does not exist, password is wrong or user is not activated"), 401

	# subscribe to topics
	if user.registrationToken:
		if user.isActivated:
			fcm.subscribe_to_topic("/topics/member", user.registrationToken)
		if user.isAdmin:
			fcm.subscribe_to_topic("/topics/admin", user.registrationToken)

	access_token = generate_access_token(user)
	return jsonify({"token": access_token})

@bp.route('/logout', methods=["DELETE"], strict_slashes=False)
@only('member')
def logout():
	access_token = request.headers.get("Authorization")
	token_data = decode_access_token(access_token)
	dbToken = Token.get(token_data['jti'])
	dbToken.delete()

	# unsubscribe from topics
	user = User.get(token_data['username'])
	if user is not None and user.registrationToken:
		if user.isActivated:
			fcm.unsubscribe_from_topic("/topics/member", user.registrationToken)
		if user.isAdmin:
			fcm.unsubscribe_from_topic("/topics/admin", user.registrationToken)
	
	return jsonify(message="Logout successful"), 200

def generate_access_token(user):
	with open(os.path.join(os.path.dirname(__file__), "../keys/private.pem"), 'rb') as f:
		private_key = f.read()
		
	tokenID = uuid.uuid4().hex

	payload = {
		"username": user.username,
		"iat": datetime.utcnow(),
		"jti": tokenID
	}

	dbToken = Token(tokenID, user.username)
	dbToken.save()

	access_token = jwt.encode(payload, private_key, algorithm='RS256')
	return access_token.decode()



def decode_access_token(access_token):
	with open(os.path.join(os.path.dirname(__file__), "../keys/public.pem"), 'rb') as f:
		public_key = f.read()

	try:
		decoded_token = jwt.decode(access_token.encode(), public_key, algorithm='RS256')
	except:
		return None
	return decoded_token