FlickrImageViewer
=================
TODO:-
1> Update the UI to use Fragments. This change will include the chage in layout when in landscape mode.
2> Write logic to manage disk cache. Currenlty the disk cache will keep on growing.
    Thoughts:- Create a thread that will go and check the validity of the images according to time. We can then delete the images
               which are 2 days old.
3> Write better logic to handle no network connection scenario.
4> Add new feature to support offline viewing of images from cache. (Need to maitain a different cache for thumbnails and a 
  different cache for full size images.)
