package dadm.frba.utn.edu.ar.quehaceres.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.squareup.picasso.Picasso
import dadm.frba.utn.edu.ar.quehaceres.OnTaskAssigned
import dadm.frba.utn.edu.ar.quehaceres.OnTaskCreated
import dadm.frba.utn.edu.ar.quehaceres.R
import dadm.frba.utn.edu.ar.quehaceres.api.Api
import dadm.frba.utn.edu.ar.quehaceres.fragments.AvailableTasksFragment
import dadm.frba.utn.edu.ar.quehaceres.fragments.CreateTaskDialog
import dadm.frba.utn.edu.ar.quehaceres.fragments.MyTasksFragment
import dadm.frba.utn.edu.ar.quehaceres.services.Services
import kotlinx.android.synthetic.main.activity_group.*
import org.greenrobot.eventbus.EventBus
import java.lang.IllegalStateException

class GroupActivity : AppCompatActivity(), AvailableTasksFragment.Listener, MyTasksFragment.Listener {

    val services by lazy { Services(this) }
    var group: Api.Group? = null
    private var eventBus = EventBus.getDefault()
    private var takingPhotoFromTask: Api.Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        group = intent.getParcelableExtra("GROUP")

        if (group == null) {
            throw IllegalStateException("Group cannot be null")
        }

        setContentView(R.layout.activity_group)
        setUpViews()
    }

    private fun setUpViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = group!!.name

        setUpAdapter()

        new_task.setOnClickListener {
            CreateTaskDialog(this, 200, ::createTask).show()
        }
    }

    @SuppressLint("CheckResult")
    private fun createTask(name: String, reward: Int) {
        services.createTask(group!!.id, name, reward)
                .subscribe(
                        { eventBus.post(OnTaskCreated()) },
                        { it.printStackTrace() }
                )
    }

    private fun setUpAdapter() {
        container.adapter = SectionsPagerAdapter(supportFragmentManager)

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        container.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(p0: Int) {
                if (p0 == 0) new_task.show()
                else new_task.hide()
            }

            override fun onPageScrollStateChanged(p0: Int) {
            }
        })
    }

    private fun onVerifyClicked(item: Api.Task) {
        takingPhotoFromTask = item
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // TODO
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
            }
        } else {
            startVerification()
        }
    }

    private fun startVerification() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK -> {
                @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                val imageBitmap = data!!.extras.get("data") as Bitmap
                confirmImage(imageBitmap, takingPhotoFromTask!!)
            }
        }
        takingPhotoFromTask = null
    }

    /*
    This is probably the most disgusting code you'll ever see
    ...But it's 3 am and i'm just too tired for this shit
     */
    @SuppressLint("CheckResult")
    private fun confirmImage(imageBitmap: Bitmap, actualTask: Api.Task) {
        val layout = RelativeLayout(this)
        val image = ImageView(this)
        val progress = ProgressBar(this)
        layout.addView(image)
        layout.addView(progress)
        image.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        progress.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        image.visibility = View.GONE

        var callback = { Toast.makeText(this, "Please wait for the upload to finish", Toast.LENGTH_SHORT).show() }
        val verification: (String) -> () -> Unit = { url: String ->
            {
                services.verifyTask(group!!.id, actualTask.id, url)
                        .subscribe(
                                { Toast.makeText(this, "Task verified", Toast.LENGTH_SHORT).show() },
                                { Toast.makeText(this, "Error verificating task", Toast.LENGTH_SHORT).show() }
                        )
            }
        }

        services.upload(imageBitmap)
                .subscribe(
                        {
                            progress.visibility = View.GONE
                            image.visibility = View.VISIBLE
                            Picasso.get().load(it.file).into(image)
                            callback = verification(it.file)
                        },
                        {
                            Toast.makeText(this, "Error while uploading an image", Toast.LENGTH_SHORT).show()
                        }
                )

        AlertDialog.Builder(this)
                .setTitle("Esta imagen es correcta?")
                .setView(layout)
                .setPositiveButton("Confirmar") { _, _ -> callback() }
                .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startVerification()
                } else {
                    Toast.makeText(this, "No tenemos permiso", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_group, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_notifications) {
            val intent = NotificationsActivity.newIntent(this)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onMyTaskClicked(item: Api.Task) {
        AlertDialog.Builder(this)
                .setTitle(item.name)
                .setMessage("Querés verificar esta tarea?")
                .setPositiveButton("Verificar") { _, _ -> onVerifyClicked(item) }
                .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
                .show()
    }

    override fun onAvailableTaskClicked(item: Api.Task) {
        AlertDialog.Builder(this)
                .setTitle(item.name)
                .setMessage("Querés asignarte esta tarea?")
                .setPositiveButton("Asignar") { _, _ -> assignTask(item) }
                .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
                .show()
    }

    @SuppressLint("CheckResult")
    private fun assignTask(item: Api.Task) {
        services.assignTask(group!!.id, item.id)
                .subscribe(
                        {
                            eventBus.post(OnTaskAssigned())
                        },
                        {
                            it.printStackTrace()
                        }
                )
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST = 1001
        const val CAPTURE_IMAGE_REQUEST = 1002

        fun newIntent(group: Api.Group, context: Context): Intent {
            val intent = Intent(context, GroupActivity::class.java)
            intent.putExtra("GROUP", group)
            return intent
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> AvailableTasksFragment.newInstance(group!!.id)
                else -> MyTasksFragment.newInstance(group!!.id)
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }
}
