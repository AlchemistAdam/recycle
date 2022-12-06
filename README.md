## What is Recycle?

Recycle is a tiny, specialized stack API for retaining and reusing short-lived, reusable objects.

## How to use it

Recycle is built around specialized stacks called *recyclers*. Like stacks, recyclers can push and pop elements. These operations are named *free* and *get* respectively. Unlike traditional stacks, recyclers can return elements even if they are empty. Additionally, recyclers use *retention policies*, which determine how recyclers retain their elements. The API provides default implementations of both recyclers and retention policies, while allowing developers to write their own implementations tailored to their own requirements if needed. The general usecase would be to obtain a recycler from the `Recyclers` factory, then obtain an element with `get` (pop it from the stack) and recycle it with `free` when it is no longer used (push it back unto the stack). The code example below shows this scenario in Java code.

## Example

A short code example of how to use the API:

```
// create Recycler
Recycler<Point> recycler = Recyclers.createLinear(Point.class, Point::new);

// get a potentially recycled Point object
Point point = recycler.get();

// do something with point here
point.x = 10;
methodUsingPoint(point);

// recycle point object so it can be reused
recycler.free(point);
```
