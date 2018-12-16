package de.unihannover.se.tauben2.repository

import android.util.Log
import androidx.lifecycle.Observer
import de.unihannover.se.tauben2.AppExecutors
import de.unihannover.se.tauben2.LiveDataRes
import de.unihannover.se.tauben2.model.network.Resource

/**
 * A generic class for making asynchronous requests that expect data from the server and write
 * it to the database
 */
abstract class AsyncDataRequest<ResultType, RequestType>(private val appExecutors: AppExecutors) {

    companion object {
        private val LOG_TAG = AsyncDataRequest::class.java.simpleName
    }

    fun send(objects: RequestType) {
        val apiResponse = createCall(objects)

        apiResponse.observeForever(object : Observer<Resource<ResultType>> {
            override fun onChanged(response: Resource<ResultType>?) {
                response?.let { resp ->
                    if (resp.status.isSuccessful()) {
                        appExecutors.diskIO().execute {
                            val result = resp.data
                            result?.let {
                                saveCallResult(it)
                                Log.d(LOG_TAG, "inserted $it into db")

                                // data successfully added to database, can remove observer
                                appExecutors.mainThread().execute {
                                    apiResponse.removeObserver(this)
                                }
                            }
                        }
                    } else if (resp.hasError()) {
                        if (resp.message == null)
                            Log.e(LOG_TAG, "An unknown error occurred.")
                        else
                            Log.e(LOG_TAG, resp.message)

                    }
                }
            }
        })
    }

    /**
     * saves the server's answer to the database
     * @param resultData data received from server
     */
    protected abstract fun saveCallResult(resultData: ResultType)

    /**
     * creates the api call
     * @param requestData data that should be attached to the requests body
     */
    protected abstract fun createCall(requestData: RequestType): LiveDataRes<ResultType>

}