# iOS reference project

This project demonstrates how a simple QR scanner can be implemented, to be used together 
with `Campus QR` project as a native iOS solution.

There are two functions that are important to demonstrate integration with this project:

- `analyzeQrCode(qrCodeValue: String)`:
Analyze the given qr code and trigger a check-in if it's a valid Campus QR code.

- `checkIn(baseUrl: String, locationId: String, email: String)`:
Sends a post request to the backend, linking location with email address.
With this solution, you can provide the email address, that you store on your app, so that user
can just scan the code, without the need to input it. 

You can use a dispatching strategy to enable an offline check-in, in this case you should add a `date` field to the request. 