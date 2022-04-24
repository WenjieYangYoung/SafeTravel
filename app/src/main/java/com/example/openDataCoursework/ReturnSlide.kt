package com.example.openDataCoursework

import android.os.Bundle
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.appcompat.app.AppCompatActivity

open class ReturnSlide : AppCompatActivity() {
    //记录手指按下时的横坐标。
    //Record the abscissa when the finger is pressed.
    private var xDown = 0f

    //记录手指按下时的纵坐标。
    //Record the ordinate when the finger is pressed.
    private var yDown = 0f

    //记录手指移动时的横坐标。
    //Record the abscissa when the finger moves.
    private var xMove = 0f

    //记录手指移动时的纵坐标。
    //Record the ordinate when the finger moves.
    private var yMove = 0f

    //用于计算手指滑动的速度。
    //Used to calculate the speed of finger sliding.
    private var mVelocityTracker: VelocityTracker? = null
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        createVelocityTracker(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                xDown = event.rawX
                yDown = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                xMove = event.rawX
                yMove = event.rawY
                //滑动的距离
                //the sliding distance
                val distanceX = (xMove - xDown).toInt()
                val distanceY = (yMove - yDown).toInt()
                //获取瞬时速度
                //Get the instantaneous speed
                val ySpeed = scrollVelocity
                val xSpeed = x_scrollVelocity
                //关闭Activity需满足以下条件：
                //The following conditions must be met to close Activity:
                //1. x轴滑动的距离>XDISTANCE_MIN
                //1. The sliding distance of the x axis > XDISTANCE_MIN.
                //zhao1.y轴滑动的距离在YDISTANCE_MIN范围内
                //2. Y-axis sliding distance is within YDISTANCE_MIN.
                //3. y轴上（即上下滑动的速度）<XSPEED_MIN，如果大于，则认为用户意图是在上下滑动而非左滑结束Activity
                //3. The sliding speed on the y axis (i.e. the speed of sliding up and down) < XSPEED_MIN.
                // if the sliding speed on the y axis is greater than YSPEED_MIN,
                // it is considered that the user's intention is to slide up and down instead of sliding left to end the Activity.
                // 4. The sliding speed on X-axis is greater than XSPEED_MIN
                if (distanceX > XDISTANCE_MIN && distanceY < YDISTANCE_MIN && ySpeed < YSPEED_MIN && xSpeed > XSPEED_MIN) {
                    finish()
                }
            }
            MotionEvent.ACTION_UP -> recycleVelocityTracker()
            else -> {}
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * 创建VelocityTracker对象，并将触摸界面的滑动事件加入到VelocityTracker当中。
     * Create a VelocityTracker object and add the sliding event of the touch interface to the VelocityTracker.
     * @param event
     */
    private fun createVelocityTracker(event: MotionEvent) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
    }

    /**
     * 回收VelocityTracker对象。
     * Recycle the VelocityTracker object.
     */
    private fun recycleVelocityTracker() {
        mVelocityTracker!!.recycle()
        mVelocityTracker = null
    }

    /**
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     * @return Sliding speed, in pixels moved per second.
     */
    private val scrollVelocity: Int
        private get() {
            mVelocityTracker!!.computeCurrentVelocity(1000)
            val velocity = mVelocityTracker!!.yVelocity.toInt()
            return Math.abs(velocity)
        }

    private val x_scrollVelocity: Int
        private get() {
            mVelocityTracker!!.computeCurrentVelocity(1000)
            val velocity = mVelocityTracker!!.xVelocity.toInt()
            return Math.abs(velocity)
        }

    companion object {
        //手指上下滑动时的最小速度
        //The minimum speed when the finger slides up and down
        private const val YSPEED_MIN = 1000

        //手指左右滑动时的最小速度
        //The minimum speed when the finger slides left and right
        private const val XSPEED_MIN = 1500

        //手指向右滑动时的最小距离
        //The minimum distance when the finger slides to the right
        private const val XDISTANCE_MIN = 250

        //手指向上滑或下滑时的最小距离
        //The minimum distance when the finger slides up or down
        private const val YDISTANCE_MIN = 100
    }
}