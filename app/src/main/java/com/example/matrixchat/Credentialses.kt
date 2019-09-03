package com.example.matrixchat

import org.matrix.androidsdk.data.Room
import org.matrix.androidsdk.rest.model.login.Credentials

object Credentialses {
    //плохой тон,по хорошему надо всё хранить в префах или в бд
    var creditntialss: Credentials?=null
    var matrixId:String?=null
    var messages:ArrayList<com.example.matrixchat.model.Message>?= arrayListOf()
    var memberCount:Int?=null
    var currentTopic:String?=null
    var nameRoom:String?=null
    var userId:String?=null
    var room: Room?=null
}