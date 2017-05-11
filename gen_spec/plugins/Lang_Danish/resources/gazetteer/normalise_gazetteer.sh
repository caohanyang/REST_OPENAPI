#!/bin/sh

sed -i.bak -e 's/å/aa/g' -e 's/æ/ae/g' -e 's/ø/oe/g' -e 's/Å/Aa/g' -e 's/Ø/Oe/g' -e 's/Æ/Ae/g' $1
rm $1.bak
