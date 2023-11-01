package com.example.a113project

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    //用來標示logcat的標籤
    //例如：2023-10-27 01:01:01.588  8555-8555  MainActivity            com.example.myapplication            D  onResume
    private val logTag = "MainActivity"
    private var counter = 0
    private var counterText: TextView? = null
    private var addButton: Button? = null
    private var subButton: Button? = null
    private var resetButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //引用父類別的onCreate
        super.onCreate(savedInstanceState)
        //以下都是新增的onCreate function
        //開啟layout
        setContentView(R.layout.activity_main)
        /*
        Context指程式運作的場景(環境)，Activity、Service皆是一個場景，更換場景時，上個場景的生命週期即結束，也會同時關閉旗下所以物件
        getApplicationContext()：場景生命週期為整個app，直到關閉、this：場景生命週期為此Activity
        詳細解釋參考：https://www.jianshu.com/p/94e0f9ab3f1d
         */
        //toast(Context,顯示字串,顯示時間)
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show()
        Log.d(logTag,"onCreate")
        findViews()
        addButton?.setOnClickListener{
            counter++
            counterText?.text = counter.toString()
        }
        addButton?.setOnLongClickListener{
            counter++
            counterText?.text = counter.toString()
            Toast.makeText(this,"This is a long click",Toast.LENGTH_SHORT).show()
            true
        }
        subButton?.setOnClickListener{
            counter--
            counterText?.text = counter.toString()
        }
        resetButton?.setOnClickListener{
            counter = 0
            counterText?.text = counter.toString()
        }
    }
    private fun findViews(){
        counterText = findViewById(R.id.textView)
        counterText?.text = "0"
        addButton = findViewById(R.id.add_button)
        addButton?.text = "add"
        subButton = findViewById(R.id.sub_button)
        subButton?.text = "sub"
        resetButton = findViewById(R.id.reset_button)
        resetButton?.text = "reset"
    }
    /*
    override fun onStart() {
        super.onStart()
        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show()
        Log.d(logTag,"onStart")
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show()
        Log.d(logTag,"onResume")
    }

    override fun onPause() {
        super.onPause()
        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show()
        Log.d(logTag,"onPause")
    }

    override fun onStop() {
        super.onStop()
        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show()
        Log.d(logTag,"onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show()
        Log.d(logTag,"onDestroy")
    }
    */
}