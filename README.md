# XMind to Latex
XMind to Latex is a tool that reads a XMind mind map and derives a structured latex document. 
This way, chapters of a book may be structured as a mind map that is then exported to latex.   

## Release
The latest XMind to Latex release can be found [here](https://github.com/arnehaber/xmind2latex/releases/latest)

## Build Status
[![Build Status](https://travis-ci.org/arnehaber/xmind2latex.svg?branch=master)](https://travis-ci.org/arnehaber/xmind2latex)
[![Coverage Status](https://coveralls.io/repos/arnehaber/xmind2latex/badge.png?branch=master)](https://coveralls.io/r/arnehaber/xmind2latex?branch=master)

## Version History
### 1.3.0

### 1.2.0
* Adds version parameter (issue [#11](https://github.com/arnehaber/xmind2latex/issues/11)).
* UTF-8 output encoding (issue [#10](https://github.com/arnehaber/xmind2latex/issues/10)).
* Adds a builder to configure the exporter (issue [#9](https://github.com/arnehaber/xmind2latex/issues/9)).
* Escape line breaks in printed latex comments (issue [#8](https://github.com/arnehaber/xmind2latex/issues/8)).
* Adds check for missing parameter arguments (issue [#7](https://github.com/arnehaber/xmind2latex/issues/7)).

### 1.1.0
* Read xml as well as XMind source files using an input stream to omit extraction of XMind files (issue [#2](https://github.com/arnehaber/xmind2latex/issues/2)). 
* Removed '.sh' from linux execution script (issue [#3](https://github.com/arnehaber/xmind2latex/issues/3)).
* Added parameter 't' to control until which level templates are used (issue [#4](https://github.com/arnehaber/xmind2latex/issues/4)).
* Undefined entries are now indented according to their level (issue [#5](https://github.com/arnehaber/xmind2latex/issues/5)).
* Set +x flag of the generated Linux execution script (issue [#6](https://github.com/arnehaber/xmind2latex/issues/6)).

### 1.0.0 
* initial release

## License

    Copyright 2014 Arne Haber
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

A copy of the complete license can be found [here](http://www.apache.org/licenses/LICENSE-2.0 "License").

All test input files are downloaded from [http://www.xmind.net/share/](http://www.xmind.net/share/).
