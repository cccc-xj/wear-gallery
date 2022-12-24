/**
 * Copyright (C) 2020 Chenhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cc.chenhe.weargallery.ui.sendimages

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import cc.chenhe.weargallery.R
import cc.chenhe.weargallery.common.ui.SimpleItemDecoration
import cc.chenhe.weargallery.common.util.HUA_WEI
import cc.chenhe.weargallery.common.util.checkHuaWei
import cc.chenhe.weargallery.common.util.checkPermission
import cc.chenhe.weargallery.common.util.isAlwaysDenied
import cc.chenhe.weargallery.databinding.AtySendImagesBinding
import cc.chenhe.weargallery.service.SendPicturesService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt


class SendImagesAty : AppCompatActivity() {

    private lateinit var binding: AtySendImagesBinding
    private val model by viewModel<SendImagesViewModel> { parametersOf(this.intent) }

    private lateinit var adapter: SendImagesAdapter

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted && isAlwaysDenied(Manifest.permission.POST_NOTIFICATIONS)) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }.also { intent ->
                    startActivity(intent)
                }
            }
            if (granted)
                startSendImagesAndFinish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkHuaWei()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_hw_title)
                .setMessage(R.string.app_hw_message)
                .setPositiveButton(R.string.app_hw_view) { _, _ ->
                    val intent = Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse(HUA_WEI)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton(R.string.app_hw_exit) { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
            return
        }

        binding = AtySendImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolBar()
        binding.targetDevice.apply {
            // make edit text do not intercept its parent's onClick event
            movementMethod = null
            keyListener = null
        }
        binding.targetDeviceLayout.setOnClickListener {
            showTargetDeviceEditDialog()
        }

        binding.targetFolder.apply {
            movementMethod = null
            keyListener = null
        }
        binding.targetFolderLayout.setOnClickListener {
            showTargetFolderEditDialog()
        }

        adapter = SendImagesAdapter(this)
        binding.recyclerView.also { rv ->
            rv.addItemDecoration(SimpleItemDecoration(this, R.dimen.folders_grid_padding))
            rv.adapter = adapter
        }
        binding.recyclerView.post {
            setSpanCount((binding.recyclerView.width.toFloat() / model.columnWidth.value!!).roundToInt())
        }

        binding.send.setOnClickListener {
            val targetNode = model.targetNode.value
            if (targetNode == null) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.tip)
                    .setMessage(R.string.send_images_select_device)
                    .setPositiveButton(R.string.confirm, null)
                    .show()
                return@setOnClickListener
            }
            if (!checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.tip)
                    .setMessage(R.string.send_images_need_notification_permission)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        requestNotificationPermissionLauncher
                            .launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton(R.string.send_images_need_notification_permission_skip) { _, _ ->
                        startSendImagesAndFinish()
                    }
                    .show()
            } else {
                startSendImagesAndFinish()
            }
        }

        model.columnWidth.observe(this) { itemWidth ->
            if (binding.recyclerView.isLaidOut) {
                setSpanCount((binding.recyclerView.width.toFloat() / itemWidth).roundToInt())
            }
        }

        model.images.observe(this) { images ->
            adapter.submitList(images)
            val num = images?.size ?: 0
            binding.header.subtitleTextView.text =
                resources.getQuantityString(R.plurals.send_images_subtitle, num, num)
        }

        model.targetFolder.observe(this) { targetFolder ->
            if (targetFolder == null) {
                binding.targetFolder.setText(R.string.send_images_directory_default)
            } else {
                binding.targetFolder.setText(targetFolder)
            }
        }

        model.targetNode.observe(this) { targetNode ->
            binding.targetDevice.setText(targetNode?.displayName)
        }
    }

    private fun startSendImagesAndFinish() {
        model.targetNode.value?.let { targetNode ->
            model.images.value?.also { images ->
                SendPicturesService.add(
                    this,
                    images,
                    targetNode,
                    model.targetFolder.value
                )
            }
        }
        finish()
    }

    private fun setSpanCount(count: Int) {
        val lm = binding.recyclerView.layoutManager as? GridLayoutManager
        if (lm == null) {
            binding.recyclerView.layoutManager = GridLayoutManager(this, count)
        } else {
            if (lm.spanCount != count) {
                lm.spanCount = count
            }
        }
    }

    private fun setupToolBar() {
        setSupportActionBar(binding.header.toolbar)
        supportActionBar!!.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.header.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.header.root.setTitle(R.string.share_image_label)
    }

    private fun showTargetDeviceEditDialog() {
        val nodes = model.nodes.toList()
        val names = nodes.map { it.displayName }.toTypedArray()
        val checked = if (model.targetNode.value == null) {
            -1
        } else {
            var r = -1
            for (i in nodes.indices) {
                if (nodes[i] == model.targetNode.value) {
                    r = i
                    break
                }
            }
            r
        }
        MaterialAlertDialogBuilder(this)
            .setSingleChoiceItems(names, checked) { dialog, i ->
                model.setTargetNode(nodes[i])
                dialog.dismiss()
            }
            .show()
    }

    private fun showTargetFolderEditDialog() {
        val margin = resources.getDimensionPixelOffset(R.dimen.dialog_view_margin)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(margin, 0, margin, 0)
        }
        val et = EditText(this).apply {
            layoutParams = params
            isSingleLine = true
            setText(model.targetFolder.value)
        }
        val container = FrameLayout(this).apply {
            addView(et)
        }
        MaterialAlertDialogBuilder(this)
            .setView(container)
            .setTitle(R.string.send_images_directory)
            .setMessage(R.string.send_images_directory_description)
            .setPositiveButton(R.string.confirm) { _, _ ->
                if (!model.setTargetFolder(et.text?.toString())) {
                    Toast.makeText(
                        this@SendImagesAty,
                        R.string.send_images_directory_invalid,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

}