package me.devsaki.hentoid.fragments.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.devsaki.hentoid.databinding.DialogReaderBrowseModeChooserBinding
import me.devsaki.hentoid.fragments.BaseDialogFragment
import me.devsaki.hentoid.util.Preferences

class ReaderBrowseModeDialogFragment : BaseDialogFragment<ReaderBrowseModeDialogFragment.Parent>() {
    companion object {
        fun invoke(parent: Fragment) {
            invoke(parent, ReaderBrowseModeDialogFragment())
        }
    }

    // UI
    private var binding: DialogReaderBrowseModeChooserBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        binding = DialogReaderBrowseModeChooserBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        binding?.apply {
            chooseHorizontalLtr.setOnClickListener {
                chooseBrowseMode(Preferences.Constant.VIEWER_BROWSE_LTR)
            }
            chooseHorizontalRtl.setOnClickListener {
                chooseBrowseMode(Preferences.Constant.VIEWER_BROWSE_RTL)
            }
            chooseVertical.setOnClickListener {
                chooseBrowseMode(Preferences.Constant.VIEWER_BROWSE_TTB)
            }
        }
    }

    private fun chooseBrowseMode(browseMode: Int) {
        Preferences.setReaderBrowseMode(browseMode)
        parent?.onBrowseModeChange()
        dismiss()
    }

    interface Parent {
        fun onBrowseModeChange()
    }
}