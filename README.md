# spritz-clojure

This is a Clojure port of [therealjamper's JavaScript implementation](https://github.com/therealjampers/spritzjs) of Rivest and Schuldt's Spritz cipher.

At the moment everything required to perform the `hash` function is implemented. The encryption and decryption functions still need porting.

The functions are written to mimic their presentation in the original paper, as is the case with the JavaScript implementation.

I still have a few things to work out about how best to manage the state of the cipher. At the moment most functions take the state and return a modified version (all of them are pure).

## Usage

```clojure
(hash message hash-length)
```

## License

This implementation is distributed under the same license as the original JavaScript implementation.

The MIT License (MIT)

Copyright (c) 2014 Mark Godfrey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
