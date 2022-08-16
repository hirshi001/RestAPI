# RESTAPI
A library to help create RestAPI by using non-blocking calls and promises.
Use Jitpack to download the library or add it as a dependency.

## How to use

Create a RestFuture:
```java
RestFuture<String, String> future = RestFuture.create((future, input)->{
    // do something when .perform() is called
    //when done, call future.taskFinished(result)
        future.getExecutor().schedule(()->{
            future.taskFinished(input); // After 2 seconds, the next stage will be called
        }, 2, TimeUnit.SECONDS);
        
}); // a Rest Future which accepts a String and outputs a String
```

Add chaining callbacks to the future
This will
```java
future.then(s->System.out.println(s))
        .pauseFor(1, TimeUnit.SECONDS)
        .map(s->s.hashCode())
        .then(h->System.out.println("HashCode="+h))
        .onFailure(cause->cause.printStackTrace())
        .perform(); // This will call the first stage of the future
```