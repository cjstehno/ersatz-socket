# Notes

> This is experimental alpha code - use at your own risk.
> But, it's for testing so not too risky.

Philosophy here is similar to Ersatz in that you are testing a client library against a known endpoint that you cannot 
directly use in testing.

Single decoder - due to the nature of the incoming data (byte streams), you can only (and MUST) define a single decoder
for the incoming data. If you have a scenario where you can accept multiple incoming data formats that do not share a 
common interface, you can create separate tests for each different input type (with a different decoder).

* No "listener" type hook is needed since you can wire in anything you want in the onConnect and onMessage handlers.
* No "called(?)" verification interface is needed since you can do it with an atomic counter if you want it
* No verification step is needed (due to the above)