# Information on Secure Logging and Validation

To prevent arbitrary code execution (ACE), also known as remote code execution (RCE), it is important to ensure the sanitization or encoding of user-provided input.
Common cases where user-provided input is used is for logging and validation purposes.

## Logging
The cwa-server uses log2j as a custom logger. In its current configuration, it encodes user-provided input by default.
Therefore, using this logger is strongly recommended.

## Processing and Validation
Similarly, strings originating from the user may not be evaluated or processed without further action.

