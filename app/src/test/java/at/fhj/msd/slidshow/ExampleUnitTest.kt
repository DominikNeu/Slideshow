package at.fhj.msd.slidshow

import at.fhj.msd.slidshow.model.Feed
import at.fhj.msd.slidshow.service.Slideshow
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    val slideshow = Slideshow

    @Test
    fun feedTest() {
        // assertEquals(4, 2 + 2)
        val waterfall = Feed("This is a waterfall")
        assertEquals(waterfall.toString(), "Slide This is a waterfall")
    }

    @Test
    fun SlideshowTest() {
        val a = Feed("a")
        val b = Feed("b")
        val A = Feed("A")
        slideshow.addFeed(a)
        slideshow.addFeed(b)
        slideshow.addFeed(A)
        assertEquals(slideshow.toString(), "Slideshow: Slide A and Slide a and Slide b")
    }

    @Test
    fun SlideshowTest2() {
        val a = Feed("a")
        val b = Feed("b")
        val A = Feed("A")
        slideshow.addFeed(a)
        slideshow.addFeed(b)
        slideshow.addFeed(A)
        assertEquals(slideshow.getAmountOfSlides(), 3)
    }

}
