s3-glacier-restore
==

Restore S3 objects archived on Glacier (GLACIER Storage Class).

## Requirements

 * Maven (`sudo apt-get install maven` on ubuntu hosts)

## Setup

Create ~/.aws/credentials file with the following content:

```
[default]
aws_access_key_id=<awsAccessKey>
aws_secret_access_key=<awsSecretKey>
```

## Usage

The project will be automatically compiled on the first run.

### Restoring a single object

```
./s3-glacier-restore s3://bucket/fullObjname <expirationInDays>
```

### Recursively restoring an S3 Path

./s3-glacier-restore -r s3://bucket/prefixToRestore <expirationInDays>

### Contribute

Feel free to submit contributions via pull request.

## TODO

- Parallel restore
