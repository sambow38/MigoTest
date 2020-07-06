package com.appwellteam.library.fragment

import android.view.View

import androidx.annotation.NonNull

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@Suppress("unused")
class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    @Suppress("ProtectedInFinal")
    protected var mBottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
            bottomSheetState = newState
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
    }
}