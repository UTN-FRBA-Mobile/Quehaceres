package dadm.frba.utn.edu.ar.quehaceres.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ShareCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import dadm.frba.utn.edu.ar.quehaceres.R
import dadm.frba.utn.edu.ar.quehaceres.adapters.GroupsAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.support.v7.widget.DividerItemDecoration
import dadm.frba.utn.edu.ar.quehaceres.services.Services


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    private val services by lazy { Services(this) }

    val adapter: GroupsAdapter by lazy {
        GroupsAdapter {
            startActivity(GroupActivity.newIntent(this@MainActivity, it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            startActivity(CreateGroupActivity.newIntent(this))
        }

        recycler_view.adapter = adapter

        val itemDecor = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        recycler_view.addItemDecoration(itemDecor)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()

        services.myGroups()
            .doOnSubscribe { showLoading(true) }
            .subscribe(
                    {
                        showLoading(false)
                        adapter.groups = it
                        adapter.notifyDataSetChanged()
                    },
                    {
                        showLoading(false)
                        Toast.makeText(this, "Hubo un error al cargar los grupos", Toast.LENGTH_SHORT).show()
                    }
                )
    }

    private fun showLoading(loading: Boolean) {
        // TODO
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                val intent = MainActivity.newIntent(this)
                startActivity(intent)
            }
            R.id.nav_profile -> {

            }
            R.id.nav_settings -> {

            }
            R.id.nav_logout -> {

            }
            R.id.nav_share -> {
                val sharableText = "Bajate QUEHACERES a tu celular y empezá a administrar los quehaceres" + "de tu casa como un campeón"
                shareText(sharableText)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareText(textToShare: String){
        val mimeType : String = "text/plain"
        val title : String = "Share Quehaceres"
        ShareCompat.IntentBuilder.from(this)
                .setType(mimeType)
                .setChooserTitle(title)
                .setText(textToShare)
                .startChooser()
    }
}
