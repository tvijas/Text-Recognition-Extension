Well hello 0/  
This service recognizes text from images and optionally translates that text to choosen language.

Service has 7 http requests:

1 - /recognize <- recognizes text from image and translate that text

2 - /signup <- creates new user and sends verification code to email

3 - /confirm <- confirms user's account

4 - /login <- creates new JWT tokens

5 - /refresh <- refreshes tokens

6 - /resetPassword <- resets password

7 - /resendCode <- resends code to email

Service uses Google APIs such as: Google Translation AI and OCR.

App has Swagger documentation, unit and mock tests
