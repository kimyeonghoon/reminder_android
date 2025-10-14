package com.reminder.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.reminder.viewmodel.MapViewModel

/**
 * v1.68.0: 지도 화면
 *
 * 카카오맵 SDK를 사용하여 선택한 장소를 지도에 표시
 *
 * @param latitude 위도
 * @param longitude 경도
 * @param placeName 장소 이름
 * @param onBackClick 뒤로가기 클릭
 * @param onLocationConfirm 위치 확인 완료 (위도, 경도, 이름)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    latitude: Double,
    longitude: Double,
    placeName: String,
    onBackClick: () -> Unit,
    onLocationConfirm: (Double, Double, String) -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // 초기 위치 설정
    LaunchedEffect(latitude, longitude, placeName) {
        viewModel.setLocation(latitude, longitude, placeName)
    }

    // 위치 권한 요청
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 승인되면 현재 위치로 이동
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        viewModel.updateMapCenter(it.latitude, it.longitude)
                        kakaoMap?.moveCamera(
                            CameraUpdateFactory.newCenterPosition(
                                LatLng.from(it.latitude, it.longitude)
                            )
                        )
                    }
                }
            } catch (e: SecurityException) {
                // 권한이 없을 경우 무시
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("지도에서 위치 확인") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    // 현재 위치 버튼
                    IconButton(
                        onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) -> {
                                    // 권한이 있으면 현재 위치로 이동
                                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                    try {
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                            location?.let {
                                                viewModel.updateMapCenter(it.latitude, it.longitude)
                                                kakaoMap?.moveCamera(
                                                    CameraUpdateFactory.newCenterPosition(
                                                        LatLng.from(it.latitude, it.longitude)
                                                    )
                                                )
                                            }
                                        }
                                    } catch (e: SecurityException) {
                                        // 권한이 없을 경우 무시
                                    }
                                }
                                else -> {
                                    // 권한 요청
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "현재 위치로 이동"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // 위치 선택 확인 버튼
            Surface(
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val selectedLocation by viewModel.selectedLocation.collectAsState()

                    if (selectedLocation != null) {
                        Text(
                            text = selectedLocation!!.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val location = viewModel.selectedLocation.value
                            if (location != null) {
                                onLocationConfirm(
                                    location.latitude,
                                    location.longitude,
                                    location.name
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = viewModel.selectedLocation.value != null
                    ) {
                        Text("이 위치로 설정")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 카카오맵 MapView
            AndroidView(
                factory = { context ->
                    MapView(context).also { mv ->
                        mapView = mv
                        mv.start(
                            object : MapLifeCycleCallback() {
                                override fun onMapDestroy() {
                                    // 지도 종료
                                }

                                override fun onMapError(error: Exception?) {
                                    // 지도 에러
                                    error?.printStackTrace()
                                }
                            },
                            object : KakaoMapReadyCallback() {
                                override fun onMapReady(map: KakaoMap) {
                                    kakaoMap = map

                                    // 초기 위치로 카메라 이동
                                    val initialPosition = LatLng.from(latitude, longitude)
                                    map.moveCamera(
                                        CameraUpdateFactory.newCenterPosition(initialPosition, 15)
                                    )

                                    // 마커 추가 (간단한 텍스트 레이블)
                                    val labelManager = map.labelManager
                                    val labelStyle = LabelStyle.from()
                                    val labelStyles = LabelStyles.from(labelStyle)
                                    val styleId = labelManager?.addLabelStyles(labelStyles)

                                    if (styleId != null && labelManager != null) {
                                        val labelOptions = LabelOptions.from(initialPosition)
                                            .setStyles(styleId)
                                            .setTexts(placeName)

                                        labelManager.layer?.addLabel(labelOptions)
                                    }

                                    // 지도 클릭 이벤트 (위치 변경)
                                    map.setOnMapClickListener { _, latLng, _, _ ->
                                        // 클릭한 위치로 마커 이동 및 선택 위치 업데이트
                                        viewModel.setLocation(
                                            latLng.latitude,
                                            latLng.longitude,
                                            "선택한 위치"
                                        )

                                        // 마커 업데이트
                                        labelManager?.layer?.removeAll()
                                        val newLabelOptions = LabelOptions.from(latLng)
                                            .setStyles(styleId!!)
                                            .setTexts("선택한 위치")
                                        labelManager.layer?.addLabel(newLabelOptions)
                                    }
                                }

                                override fun getPosition(): LatLng {
                                    return LatLng.from(latitude, longitude)
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { mv ->
                    mv.finish()
                }
            )
        }
    }

    // Lifecycle 관리
    DisposableEffect(Unit) {
        onDispose {
            mapView?.finish()
        }
    }
}
