package com.lukakordzaia.streamflowphone.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.google.android.material.textfield.TextInputEditText
import com.lukakordzaia.core.network.LoadingState
import com.lukakordzaia.core.network.models.imovies.request.user.PostLoginBody
import com.lukakordzaia.core.utils.hideKeyboard
import com.lukakordzaia.streamflowphone.R
import com.lukakordzaia.streamflowphone.databinding.FragmentPhoneLoginBinding
import com.lukakordzaia.streamflowphone.ui.baseclasses.BaseFragmentPhoneVM
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment: BaseFragmentPhoneVM<FragmentPhoneLoginBinding, ProfileViewModel>() {
    override val viewModel by viewModel<ProfileViewModel>()
    override val reload: () -> Unit = {
        viewModel.userLogin(PostLoginBody(
            binding.passwordInput.text.toString(),
            binding.usernameInput.text.toString()
        ))
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPhoneLoginBinding
        get() = FragmentPhoneLoginBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBarListener(resources.getString(R.string.authorization), binding.toolbar)

        setCredentials()
        fragmentListeners()
        fragmentObservers()
    }

    private fun setCredentials() {
        if (sharedPreferences.getUsername() != "") {
            binding.usernameInput.setText(sharedPreferences.getUsername())
        }
        if (sharedPreferences.getPassword() != "") {
            binding.passwordInput.setText(sharedPreferences.getPassword())
        }
    }

    private fun fragmentListeners() {
        binding.authButton.setOnClickListener {
            clearFocus(binding.usernameInput)
            clearFocus(binding.passwordInput)

            if (!binding.usernameInput.text.isNullOrEmpty() && !binding.passwordInput.text.isNullOrEmpty()) {
                viewModel.userLogin(PostLoginBody(
                    binding.passwordInput.text.toString(),
                    binding.usernameInput.text.toString()
                ))
            }
        }

        onEditorActionListener(binding.usernameInput)
        onEditorActionListener(binding.passwordInput)
    }

    private fun fragmentObservers() {
        viewModel.generalLoader.observe(viewLifecycleOwner, {
            if (it == LoadingState.LOADED) {
                viewModel.newToastMessage(getString(R.string.authorization_is_successful))
                requireActivity().onBackPressed()
            }
        })
    }

    private fun clearFocus(view: TextInputEditText) {
        view.apply {
            hideKeyboard()
            clearFocus()
        }
    }

    private fun onEditorActionListener(view: TextInputEditText) {
        view.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    clearFocus(view)
                    return@setOnEditorActionListener true
                }
            }
            return@setOnEditorActionListener false
        }
    }
}