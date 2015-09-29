#!/bin/bash

CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

eval "openssl pkcs8 -topk8 -outform DER -in $1/key.pem -inform PEM -out $1/key.pk8 -nocrypt"
eval "java -jar signapk.jar certificate.pem $1/key.pk8 $2 $1/$2.signed.jar"