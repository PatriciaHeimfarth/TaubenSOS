from flask import (Blueprint, request, jsonify)
import boto3, uuid

from api.models.case import (Case, case_schema, cases_schema)
from api.models.injury import (Injury, injury_schema)
from api.models.medium import (Medium, medium_schema)

bp = Blueprint("case", __name__, url_prefix="/api")

@bp.route("/case", methods=["GET"], strict_slashes=False)
def read_cases():
	if request.method == "GET":
		cases = Case.all()
		result = [generate_media_urls(Case=c, ClientMethod="get_object") for c in cases]
		return jsonify(result)

@bp.route("/case", methods=["POST"], strict_slashes=False)
def create_case():
	if request.method == "POST":
		json = request.get_json()
		if json.get("media") is not None:
			json.get("media")[:] = [medium_schema.dump(Medium("photos/" + str(uuid.uuid4()) + "-" + m)).data for m in json.get("media")]
		case, errors = case_schema.load(json)
		if errors:
			return jsonify(errors), 400
		else:
			case.save()
			result = generate_media_urls(Case=case, ClientMethod="put_object")
			return jsonify(result), 201

@bp.route("/case/<caseID>", methods=["GET"], strict_slashes=False)
def read_case(caseID):
	if request.method == "GET":
		case = Case.get(caseID)
		result = generate_media_urls(Case=case, ClientMethod="get_object")
		return jsonify(result)

def generate_media_urls(Case, ClientMethod):
	result = case_schema.dump(Case).data
	s3 = boto3.client("s3")
	result.get("media")[:] = [s3.generate_presigned_url(ClientMethod=ClientMethod, Params={"Bucket": "tauben2", "Key": m.get("uri")}, ExpiresIn=600) for m in result.get("media")]
	return result