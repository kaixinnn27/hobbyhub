package com.example.hobbyhub.report.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hobbyhub.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class UserDemographicsReportFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_demographics, container, false)
    }



}