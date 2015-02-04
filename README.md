# idempotent
Trait for making a method call idempotent.

This can be used for instance for listeners listening for an event. The messaging infrastructure guarantees at-least-once delivery so it's possible that the call to the listener is done more than once.
One possibility is to make the listener itself idempotent by keeping a record of the events it handled. This is the approach used here.

See the unit test for a working example.

