package tubach.niko.hw5

// Following file courtesy of Scott Stanchfield
// https://gitlab.com/android-development-2022-refresh/google-map/-/blob/main/app/src/main/java/com/javadude/carfinder/DrawableHelpers.kt

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// NOTE: I wanted to show an example of a set of extensions nicely-documented
// for KDoc generation using the Dokka tool. There's an example of the setup
// in the build scripts. Note that Dokka is still pretty young and doesn't
// generate the prettiest documentation... But you can use it to generate
// markdown and use another tool to generate something prettier...

/**
 * Returns a [Bitmap] for the intrinsic size of any [Drawable]. If the
 * drawable is a [BitmapDrawable], we just return it. Otherwise, we create a
 * new [Bitmap] of the intrinsic size of the [Drawable], draw the [Drawable]
 * to it, and return the [Bitmap].
 *
 * @sample loadBitmap
 * @receiver Any [Drawable]
 * @return a [Bitmap] representation of the [Drawable]
 */
fun Drawable.toBitmap(): Bitmap =
    when {
        // if it's already a bitmap; just return it
        this is BitmapDrawable -> bitmap

        // otherwise, create a bitmap and draw the drawable to it
        intrinsicWidth == 0 || intrinsicHeight == 0 ->
            throw IllegalArgumentException(
                "Drawable cannot be converted to a Bitmap; it must have " +
                        "non-zero intrinsic width and height")

        else ->
            Bitmap.createBitmap(
                intrinsicWidth,
                intrinsicHeight,
                Bitmap.Config.ARGB_8888
            ).apply {
                val canvas = Canvas(this)
                setBounds(0, 0, canvas.width, canvas.height)
                draw(canvas)
            }
    }

/**
 * Loads a [Bitmap] by the resource id of a [Drawable].
 *
 * @sample loadBitmapDescriptor
 * @receiver [Context] to load the [Drawable]
 * @param id [Int] the resource id of the [Drawable]
 * @return a [Bitmap] representation of the [Drawable]
 */
fun Context.loadBitmap(@DrawableRes id: Int): Bitmap =
    ContextCompat.getDrawable(this, id)?.toBitmap()
        ?: throw Resources.NotFoundException(resources.getResourceName(id))


/**
 * Loads a [BitmapDescriptor] by the resource id of a [Drawable].
 *
 * @sample com.javadude.maps.samples.SampleActivity.onCreate
 * @receiver [Context] to load the [Drawable]
 * @param id [Int] the resource id of the [Drawable]
 * @return a [Bitmap] representation of the [Drawable]
 */
@Suppress("KDocUnresolvedReference")
// they actually are resolved by dokka 'samples' config
fun Context.loadBitmapDescriptor(@DrawableRes id: Int): BitmapDescriptor =
    loadBitmap(id).toBitmapDescriptor()


/**
 * Converts a [Bitmap] to a [BitmapDescriptor]
 *
 * @sample loadBitmapDescriptor
 */
fun Bitmap.toBitmapDescriptor(): BitmapDescriptor =
    BitmapDescriptorFactory.fromBitmap(this)