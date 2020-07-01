package com.example.migotest

import com.appwellteam.library.fragment.BaseActivity
import com.appwellteam.library.fragment.BaseFragment
import com.example.migotest.fragment.MainFragment

class MainActivity: BaseActivity() {
    override fun initMainFragment(): BaseFragment {
        return MainFragment()
    }
}