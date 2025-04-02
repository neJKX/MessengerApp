package com.messange.app.messengerapp.presenter.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.messange.app.messengerapp.databinding.FragmentChannelsBinding
import com.messange.app.messengerapp.databinding.FragmentChatListBinding


class ChannelsFragment : Fragment() {
    private lateinit var binding : FragmentChannelsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChannelsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}