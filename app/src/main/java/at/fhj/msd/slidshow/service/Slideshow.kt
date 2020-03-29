package at.fhj.msd.slidshow.service

import at.fhj.msd.slidshow.model.Feed

object Slideshow {
  var slides = mutableListOf<Feed>()

  // this implementation is not the best - better would be (perhaps) no Singleton for the Slideshow
  var filteredSlides = mutableListOf<Feed>()

  var currentFeed:Feed? = null
  var currentPosition:Int = 0

  var filterByExistingDescription:Boolean = false

  fun addFeed(aNewFeed: Feed) {
    slides.add(aNewFeed)
    aNewFeed.id // fill lazy id
  }

  override fun toString(): String {
    //val sortedFeeds = slides.sortedBy { it.title }
    val sortedFeeds = slides.sortedBy { n -> n.title }
    //return "Slideshow Slides: ${slides.joinToString (" \nand ","")}"
    return sortedFeeds.joinToString (" and ","Slideshow: ")
  }

  fun getAmountOfSlides(): Int {
    if(filterByExistingDescription) {
      return filteredSlides.size
    } else {
      return slides.size
    }
  }

  fun shuffle():Feed {
    if(filterByExistingDescription) {
      filteredSlides.shuffle()
      currentFeed = getFirstFeed()
      return getFirstFeed()
    } else {
      slides.shuffle()
      currentFeed = getFirstFeed()
      return getFirstFeed()
    }
  }

  fun getFirstFeed():Feed {
    if(filterByExistingDescription) {
      currentPosition = 0
      currentFeed = filteredSlides[0]
      return filteredSlides[0]
    } else {
      currentPosition = 0
      currentFeed = slides[0]
      return slides[0]
    }
  }

  fun getLastFeed():Feed {
    if(filterByExistingDescription) {
      currentPosition = getAmountOfSlides() - 1
      currentFeed = filteredSlides.last()
      return filteredSlides.last()
    } else {
      currentPosition = getAmountOfSlides() - 1
      currentFeed = slides.last()
      return slides.last()

    }
  }

  fun getNextFeed():Feed {
    if(currentPosition == getAmountOfSlides()-1) {
      return getFirstFeed()
    } else {
      currentPosition++
      if(filterByExistingDescription) {
        currentFeed = filteredSlides[currentPosition]
        return filteredSlides[currentPosition]
      } else {
        currentFeed = slides[currentPosition]
        return slides[currentPosition]
      }
    }
  }

  fun getPreviousFeed(): Feed {
    if(currentPosition == 0) {
      return getLastFeed()
    } else {
      currentPosition--
      if(filterByExistingDescription) {
        currentFeed = filteredSlides[currentPosition]
        return filteredSlides[currentPosition]
      } else {
        currentFeed = slides[currentPosition]
        return slides[currentPosition]
      }
    }
  }

  fun findFeedByID(id : Int): Feed {
    for (s in slides) {
      if(s.id == id) {
        return s
      }
    }
    return getFirstFeed()
  }

  fun setCurrentFeed(currentFeed: Feed, currentPosition: Int) {
    this.currentFeed = currentFeed
    this.currentPosition = currentPosition
  }

  fun checkExistingDescription() {
    var feeds = mutableListOf<Feed>()
    for(feed in slides) {
      if(!feed.imageDescription.isNullOrEmpty()) {
        feeds.add(feed)
      }
    }
    filteredSlides = feeds
  }

  fun getCurrentFeedPosition():Int {
    if(currentFeed == null) {
      return 1
    } else {
      return currentPosition+1
    }
  }

  fun sortByDate() {
    if(filterByExistingDescription) {
      filteredSlides.sortBy { n -> n.takenOnDate }
    } else {
      slides.sortBy { n -> n.takenOnDate }
    }
  }

  fun sortByTitle() {
    if(filterByExistingDescription) {
      filteredSlides.sortBy { n -> n.title }
    } else {
      slides.sortBy { n -> n.title }
    }
  }

  fun sortByID() {
    if(filterByExistingDescription) {
      filteredSlides.sortBy { n -> n.id }
    } else {
      slides.sortBy { n -> n.id }
    }
  }

  fun unsort() {
    if(filterByExistingDescription) {
      filteredSlides.sortBy { n -> n.id }
    } else {
      slides.sortBy { n -> n.id }
    }
  }

}