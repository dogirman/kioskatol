package com.example.kioskatol



import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminLoginActivity : AppCompatActivity() {
    private val adminPassword = "1234" // TODO: вынести в настройки

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        val password = findViewById<EditText>(R.id.password)
        findViewById<Button>(R.id.login_button).setOnClickListener {
            if (password.text.toString() == adminPassword) {
                stopLockTask()
                startActivity(Intent(this, AppListActivity::class.java))
            } else {
                Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
