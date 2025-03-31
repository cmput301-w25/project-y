#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 filename"
  exit 1
fi

input_file="$1"

# Remove void returns
sed -r -i 's/ : void//g' "$1"

# Remove parameter names
sed -r -i 's/, \w+ :/,/g' "$1"
sed -r -i 's/\(\w+ : /(/g' "$1"

# add space before {
sed -r -i ':a;N;$!ba;s/\{\n/ {\n/g' "$1"

# Remove lines showing inheritance to 
removeLinesContaining=("> AppCompatActivity" "> Serializable" "> ArrayAdapter" "> Parcelable")
for pattern in "${removeLinesContaining[@]}"; do
  sed -i "/${pattern}$/d" "$1"
done

# Indent and add new line above each class
tmp_file=$(mktemp)
awk '
  BEGIN {
    first_header = 1
  }
  # Detect a block header: class, enum, or interface followed by a name and an opening brace.
  /^(class|enum|interface)[[:space:]]+[[:alnum:]_]+[[:space:]]*\{$/ {
    if (!first_header) {
      print ""
    }
    first_header = 0
    print $0
    inside = 1
    next
  }
  # When inside a block, if the line is exactly "}", print it and end the block.
  inside && $0 == "}" {
    print $0
    inside = 0
    next
  }
  # For lines inside a block, indent them (with 4 spaces).
  inside {
    print "    " $0
    next
  }
  # For lines outside any block, print as is.
  { print $0 }
' "$input_file" > "$tmp_file"
mv "$tmp_file" "$input_file"
