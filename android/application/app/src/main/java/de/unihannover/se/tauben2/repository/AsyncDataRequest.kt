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
                        val result = resp.data
                        result?.let {
                            updateData(it)
                            appExecutors.mainThread().execute {
                                apiResponse.removeObserver(this)
                            }
                        }

                    } else if (resp.hasError()) {
                        if (resp.message == null)
                            Log.e(LOG_TAG, "An unknown error occurred.")
                        else
                            Log.e(LOG_TAG, resp.message)

                    } else {
                        // why do I need to do that
                    }
                }
            }
        })
    }

    /**
     * Fetches the updated item from server and inserts it into the local database
     * This is necessary because the server's response for a case only contains the upload urls
     * but not the host urls for pictures
     */
    private fun updateData(resultData: ResultType) {
        // update case in db, fetch from server

        val newItem = fetchUpdatedData(resultData)
        newItem.observeForever(object : Observer<Resource<ResultType>> {
            override fun onChanged(response: Resource<ResultType>?) {
                appExecutors.diskIO().execute {
                    response?.let { resp ->
                        if (resp.status.isSuccessful()) {
                            resp.data?.let { saveUpdatedData(it) }
                            // data successfully added to database, can remove observer
                            appExecutors.mainThread().execute {
                                newItem.removeObserver(this)
                            }
                        } else if (resp.hasError()) {
                            Log.e(LOG_TAG, resp.message)
                        }
                    }
                }
            }
        })
    }

    /**
     * fetches an instance of the item to be updated or created in db based on information from
     * resultData
     * @param resultData data received from server
     * @return
     */
    protected abstract fun fetchUpdatedData(resultData: ResultType): LiveDataRes<ResultType>

    /**
     * saves the server's answer to the database
     * @param updatedData data received from server
     */
    protected abstract fun saveUpdatedData(updatedData: ResultType)

    /**
     * creates the api call
     * @param requestData data that should be attached to the requests body
     */
    protected abstract fun createCall(requestData: RequestType): LiveDataRes<ResultType>

}