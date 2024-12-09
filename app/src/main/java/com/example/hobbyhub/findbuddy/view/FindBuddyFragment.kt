package com.example.hobbyhub.findbuddy.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.FragmentFindBuddyBinding
import com.example.hobbyhub.findbuddy.viewmodel.MapViewModel
import com.example.hobbyhub.hobby.viewmodel.HobbyViewModel
import com.example.hobbyhub.hobby.viewmodel.UserHobbyViewModel
import com.example.hobbyhub.utility.toBitmap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class FindBuddyFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var binding: FragmentFindBuddyBinding
    private lateinit var mapFragment: SupportMapFragment
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val db = FirebaseFirestore.getInstance()
    private val authViewModel: AuthViewModel by viewModels()
    private val mapViewModel: MapViewModel by viewModels()
    private val userHobbyViewModel: UserHobbyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFindBuddyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun addCustomMarker() {
        // Ensure googleMap is not null
        googleMap?.let { map ->
            // Get user's last known location
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permission
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
                return
            }
            // Add a marker for the user's current location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val userLocation = LatLng(it.latitude, it.longitude)
                        // Add marker for current user's location
                        map.addMarker(
                            MarkerOptions()
                                .position(userLocation)
                                .title("Your Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        )
                        // Move camera to current user's location
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    }
                }
        }
    }

    private fun enableMyLocation() {
        // Check and request location permission if not granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // Enable showing user's location on the map
        googleMap?.isMyLocationEnabled = true
    }

    private fun addMarkersForUsers() {
        db.collection("location")
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Retrieve current user's UID
                val currentUserUid = authViewModel.getCurrentUserId()

                // Iterate through each document (user location) in the collection
                for (document in querySnapshot.documents) {
                    // Get latitude and longitude from the document data
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    // Create LatLng object for the user's location
                    val userLatLng = LatLng(latitude, longitude)

                    // Get userId from the document
                    val userId = document.id

                    // Check if the userId is the same as the current user's UID
                    if (currentUserUid != userId) {
                        // Retrieve user data based on userId from the 'user' collection
                        db.collection("user").document(userId)
                            .get()
                            .addOnSuccessListener { userDocument ->
                                if (userDocument != null && userDocument.exists()) {
                                    // Get user's name from the 'user' document
                                    val userName = userDocument.getString("name") ?: "Unknown"

                                    // Add marker for the user's location on the map with the user's name
                                    googleMap?.addMarker(
                                        MarkerOptions()
                                            .position(userLatLng)
                                            .title(userDocument.id)
                                            .snippet("$userName's Location")
                                    )
                                } else {
                                    // User document does not exist or is null
                                    // Handle this case if needed
                                }
                            }
                            .addOnFailureListener { exception ->
                                // Handle failure to retrieve user data from 'user' collection
                                // You can log the error or display a message to the user
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to retrieve user locations from Firestore
                // You can log the error or display a message to the user
            }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val userName = marker.title
        // Show an alert dialog with user details
        if (userName != null) {
            showUserDetailsDialog(userName)
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun showUserDetailsDialog(userId: String) {
        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_details, null)

        // Initialize dialog components
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Find your hobby buddy")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                addFriend(userId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val userRef = db.collection("user").document(userId)
        userRef.get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    // User document exists, retrieve user details
                    val userName = userDocument.getString("name") ?: "Unknown"
                    val photo = userDocument.getBlob("photo")

                    lifecycleScope.launch {
                        val userHobby = userHobbyViewModel.get(userDocument.id)
                        if (userHobby != null) {
                            val preferredCategories = userHobby.preferredCategories
                            val savedHobbies =
                                userHobbyViewModel.getAllFavoriteHobbies(userDocument.id)

                            userHobbyViewModel.favoriteHobbies.observe(
                                viewLifecycleOwner,
                                Observer { hobbies ->
                                    val hobbyNames = StringBuilder("Saved Hobbies: ")
                                    hobbies.forEach { hobby ->
                                        hobbyNames.append(hobby.name).append(", ")
                                    }
                                    if (hobbyNames.isNotEmpty()) {
                                        hobbyNames.setLength(hobbyNames.length - 2)
                                    }
                                    dialogView.findViewById<TextView>(R.id.tvLearningStyle).text =
                                        hobbyNames
                                })

                            val categories = StringBuilder("Preferred Categories: ")
                            preferredCategories.forEach { category ->
                                categories.append(category.toString()).append(", ")
                            }

                            dialogView.findViewById<TextView>(R.id.tvInterests).text = categories

                        }
                    }
                    // Set user details in the dialog views
                    dialogView.findViewById<TextView>(R.id.tvName).text = "Name: $userName"

                    val ivPhoto = dialogView.findViewById<ImageView>(R.id.ivPhoto)
                    if (photo?.toBitmap() != null) {
                        ivPhoto.setImageBitmap(photo.toBitmap())
                    } else {
                        ivPhoto.setImageResource(R.drawable.ic_default_profile)
                    }

                    // Display the dialog
                    val dialog = dialogBuilder.create()
                    dialog.show()
                } else {
                    // User document doesn't exist
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to retrieve user document
                Toast.makeText(
                    requireContext(),
                    "Failed to retrieve user details",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addFriend(userId: String) {
        val currentUserUid = authViewModel.getCurrentUserId()

        if (currentUserUid != null) {
            val currentUserRef = db.collection("user").document(currentUserUid)
            val selectedUserRef = db.collection("user").document(userId)

            db.runTransaction { transaction ->
                val currentUserDoc = transaction.get(currentUserRef)
                val selectedUserDoc = transaction.get(selectedUserRef)

                val currentUserFriends =
                    currentUserDoc.get("friends") as? MutableList<String> ?: mutableListOf()
                val selectedUserFriends =
                    selectedUserDoc.get("friends") as? MutableList<String> ?: mutableListOf()

                if (!currentUserFriends.contains(userId)) {
                    currentUserFriends.add(userId)
                    transaction.update(currentUserRef, "friends", currentUserFriends)
                }

                if (!selectedUserFriends.contains(currentUserUid)) {
                    selectedUserFriends.add(currentUserUid)
                    transaction.update(selectedUserRef, "friends", selectedUserFriends)
                }
            }
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "User added as friend", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to add friend", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        addCustomMarker()
        enableMyLocation()
        addMarkersForUsers()
        googleMap?.setOnMarkerClickListener(this)
    }
}