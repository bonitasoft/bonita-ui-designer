REM This file is needed by npm-frontend-plugin since due to a bug, this plugin doesn't work whithout local node installation for our need
REM https://github.com/eirslett/frontend-maven-plugin/issues/204

@echo off
%~dp0node/npm %*
@echo on
