package com.example.businessbase.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.businessbase.R
import com.example.businessbase.databinding.BaseDialogPrimitiveBinding
import com.example.businessbase.utils.dp2px

private const val ARG_PARAM_TITLE = "ARG_PARAM_TITLE"
private const val ARG_PARAM_POSITION_STR = "ARG_PARAM_POSITION_STR"

class PrimitiveDialog(
    private var mPositionBtnClickListen: (() -> Unit),
) : DialogFragment() {
    private lateinit var mBinding: BaseDialogPrimitiveBinding

    private var mTitle: String? = null

    private var mPositionBtnStr: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setStyle(STYLE_NORMAL, R.style.base_Dialog_Theme)
        setStyle(STYLE_NO_FRAME, R.style.base_Dialog_Theme_Light)
        arguments?.let {
            mTitle = it.getString(ARG_PARAM_TITLE)
            mPositionBtnStr = it.getString(ARG_PARAM_POSITION_STR)
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            dp2px(requireContext(), 275.3F),
            dp2px(requireContext(), 137.7F)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding = BaseDialogPrimitiveBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        mBinding.tvTitle.text = if (mTitle.isNullOrBlank()) {
            resources.getString(R.string.base_str_dialog_title)
        } else {
            mTitle
        }
        mBinding.btnSelectOk.text = if (mPositionBtnStr.isNullOrBlank()) {
            resources.getString(R.string.base_str_dialog_continue)
        } else {
            mPositionBtnStr
        }
        mBinding.btnSelectOk.setOnClickListener {
            mPositionBtnClickListen.invoke()
            dismissAllowingStateLoss()
        }

        mBinding.btnSelectCancel.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    companion object {
        private const val TAG = "PrimitiveDialog"

        @JvmStatic
        fun showDialog(
            fm: FragmentManager,
            title: String = "",
            positionBtnStr: String = "",
            positionBtnClickListen: (() -> Unit)
        ) {
            PrimitiveDialog(positionBtnClickListen).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM_TITLE, title)
                    putString(ARG_PARAM_POSITION_STR, positionBtnStr)
                }
                show(fm, TAG)
            }
        }
    }
}