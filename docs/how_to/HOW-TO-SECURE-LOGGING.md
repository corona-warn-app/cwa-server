# Information on Secure Logging and Validation

To prevent arbitrary code execution (ACE), also known as remote code execution (RCE), it is important to ensure the sanitization or encoding of user-provided input.
Common cases where user-provided input is used is for logging and for data processing purposes.

## Logging and Exception Handling

The cwa-server uses log4j as its standard logger. In its current configuration, it encodes user-provided input by default.
Therefore, using this logger is strongly recommended.
Furthermore, proper exception handling is important. It has to be made sure, that unvalidated user-provided input is never part of an exception message.
This can be achieved by logging all occurring exceptions with log4j.
As an example:
`log4j.error("Federation batch processing for date {} and batchTag {} failed. Status set to {}.",
date, batchTag, errorStatus.name(), exception.getMessage());`.

## Processing and Validation

Similarly, strings originating from the user may not be evaluated or processed without further action.
Queries should never be parameterized with unvalidated user input. Always use libraries, formatters, and validators that are commonly known as safe before processing user input or handing it to the database layer.
