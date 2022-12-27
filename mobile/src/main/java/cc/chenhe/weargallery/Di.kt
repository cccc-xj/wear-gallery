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

package cc.chenhe.weargallery

import android.content.Intent
import cc.chenhe.weargallery.common.jsonadapter.UriAdapter
import cc.chenhe.weargallery.repo.CheckUpdateRepo
import cc.chenhe.weargallery.ui.folderimages.FolderImagesViewModel
import cc.chenhe.weargallery.ui.folders.FoldersViewModel
import cc.chenhe.weargallery.ui.imagedetail.ImageDetailViewModel
import cc.chenhe.weargallery.ui.images.ImagesViewModel
import cc.chenhe.weargallery.ui.legacy.SharedViewModel
import cc.chenhe.weargallery.ui.main.MainScreenViewModel
import cc.chenhe.weargallery.ui.sendimages.SendImagesViewModel
import cc.chenhe.weargallery.utils.NotificationChecker
import com.squareup.moshi.Moshi
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory { Moshi.Builder().add(UriAdapter()).build() }
    factory { CheckUpdateRepo(androidContext(), get()) }
    factory { NotificationChecker(androidContext()) }

    viewModel { MainScreenViewModel(androidApplication(), get()) }
    viewModel { SharedViewModel(androidApplication()) }
    viewModel { ImagesViewModel(androidApplication()) }
    viewModel { FoldersViewModel(androidApplication()) }
    viewModel { (bucketId: Int) -> FolderImagesViewModel(androidApplication(), bucketId) }
    viewModel { (bucketId: Int) -> ImageDetailViewModel(androidApplication(), bucketId) }
    viewModel { (intent: Intent) -> SendImagesViewModel(androidApplication(), get(), intent) }
}