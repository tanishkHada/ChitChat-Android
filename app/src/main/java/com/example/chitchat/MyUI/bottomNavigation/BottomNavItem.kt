package com.example.chitchat.MyUI.bottomNavigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import com.example.chitchat.R

sealed class BottomNavItem (
    val label : String,
    val route : String
){
    object Chats : BottomNavItem(
        "Chats",
        "chats"
    )

    object Status : BottomNavItem(
        "Status",
        "status"
    )

    object Profile : BottomNavItem(
        "Profile",
        "profile"
    )

    @Composable
    fun getSelectedIcon() : Painter{
        return when(this){
            is Chats -> painterResource(id = R.drawable.chat_filled)
            is Status -> painterResource(id = R.drawable.status_filled)
            is Profile -> painterResource(id = R.drawable.profile_filled)
        }
    }

    @Composable
    fun getUnselectedIcon() : Painter{
        return when(this){
            is Chats -> painterResource(id = R.drawable.chat_outlined)
            is Status -> painterResource(id = R.drawable.status_outlined)
            is Profile -> painterResource(id = R.drawable.profile_outlined)
        }
    }
}