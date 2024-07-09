package com.example.chitchat.MyUtils

import com.example.chitchat.model.Status
import com.example.chitchat.model.User
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

object Utilities {
    fun formatTimestamp(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val sdf = SimpleDateFormat("HH:mm MMM dd, yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    fun getStatusStructure(lst : List<Status>): Map<User, List<Pair<String, String>>>{
        //map of users -to- all their status objects
        val usersMap : Map<User, List<Status>> = lst.groupBy { it.user }

        //iterate over userMap and sort each user's status list in ascending order based on timestamp, so that latest
        //appears at last
        val sortedUserStatusLists : Map<User, List<Status>> = usersMap.mapValues {(_, statuses) ->
            statuses.sortedBy {
                it.timeStamp
            }
        }

        //sort the users in descending order on the basis of timestamp of last item of list of status object
        val sortedUsers : List<Map.Entry<User, List<Status>>> = sortedUserStatusLists.entries.sortedByDescending {
            it.value.lastOrNull()?.timeStamp
        }

//        //create map of user and its list of, statusImageUris sorted in ascending order of timestamp
//        val userStatusImageUriMap : Map<User, List<String>> = sortedUsers.associate {
//            it.key to it.value.map {
//                it.statusImageUri
//            }
//        }

        // Create a map of user and its list of statusImageUris and their timestamps converted to strings in HH:mm format
        val userStatusImageUriMap: Map<User, List<Pair<String, String>>> = sortedUsers.associate { userStatusEntry ->
            userStatusEntry.key to userStatusEntry.value.map { status ->
                Pair(status.statusImageUri, status.timeStamp.toStringFormat())
            }
        }

        return userStatusImageUriMap
    }
}

// Function to convert Firebase Timestamp to String with format HH:mm
fun Timestamp.toStringFormat(pattern: String = "HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(this.toDate())
}
