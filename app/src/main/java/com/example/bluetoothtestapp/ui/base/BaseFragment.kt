package com.example.bluetoothtestapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VBinding : ViewBinding> : Fragment() {

    protected lateinit var binding: VBinding
    protected abstract fun getViewBinding(): VBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setUp()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        setOnClickListener()
    }

    open fun setUp() {}

    open fun observeData() {}

    open fun setOnClickListener() {}

    fun navigateUp() {
        requireActivity().onBackPressed()
    }

    fun launchFragment(resId: Int) {
        findNavController().navigate(resId)
    }

    fun launchFragment(action: NavDirections) {
        findNavController().navigate(action)
    }

    private fun init() {
        binding = getViewBinding()
    }
}
