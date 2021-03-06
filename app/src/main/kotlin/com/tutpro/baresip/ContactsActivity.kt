package com.tutpro.baresip

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ListView

class ContactsActivity : AppCompatActivity() {

    internal lateinit var clAdapter: ContactListAdapter
    internal lateinit var aor: String
    private var lastClick: Long = 0

    public override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        aor = intent.getStringExtra("aor")!!
        Utils.addActivity("contacts,$aor")

        val listView = findViewById(R.id.contacts) as ListView
        clAdapter = ContactListAdapter(this, Contact.contacts(), aor)
        listView.adapter = clAdapter
        listView.isLongClickable = true

        val plusButton = findViewById(R.id.plusButton) as ImageButton
        plusButton.setOnClickListener {
            if (Contact.contacts().size >= Contact.CONTACTS_SIZE) {
                Utils.alertView(this, getString(R.string.notice),
                        String.format(getString(R.string.contacts_exceeded),
                                Contact.CONTACTS_SIZE))
            } else {
                if (SystemClock.elapsedRealtime() - lastClick > 1000) {
                    lastClick = SystemClock.elapsedRealtime()
                    val i = Intent(this, ContactActivity::class.java)
                    val b = Bundle()
                    b.putBoolean("new", true)
                    b.putString("uri", "")
                    i.putExtras(b)
                    startActivityForResult(i, MainActivity.CONTACT_CODE)
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) clAdapter.notifyDataSetChanged()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            android.R.id.home -> {
                BaresipService.activities.remove("contacts,$aor")
                val i = Intent()
                setResult(Activity.RESULT_OK, i)
                finish()
            }
        }

        return true

    }

    override fun onBackPressed() {

        BaresipService.activities.remove("contacts,$aor")
        val i = Intent()
        setResult(Activity.RESULT_OK, i)
        finish()
        
    }

    companion object {

        fun findContactURI(name: String): String {
            for (c in Contact.contacts())
                if (c.name == name)
                    return c.uri.removePrefix("<")
                            .replaceAfter(">", "")
                            .replace(">", "")
            return name
        }

        fun findContact(uri: String): Contact? {
            for (c in Contact.contacts())
                if ((Utils.uriUserPart(c.uri) == Utils.uriUserPart(uri)) &&
                        (Utils.uriHostPart(c.uri) == Utils.uriHostPart(uri)))
                    return c
            return null
        }

        fun nameExists(name: String, ignoreCase: Boolean): Boolean {
            for (c in Contact.contacts())
                if (c.name.equals(name, ignoreCase = ignoreCase)) return true
            return false
        }

        fun contactName(uri: String): String {
            for (c in Contact.contacts())
                if ((Utils.uriUserPart(c.uri) == Utils.uriUserPart(uri)) &&
                        (Utils.uriHostPart(c.uri) == Utils.uriHostPart(uri)))
                    return c.name
            return uri
        }
    }
}
