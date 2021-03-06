local string = require("string")
local term = require("term")
local writer = require("component").os_cardwriter

local data
local name
local lock
local doLock

term.clear()
term.setCursor(1,1)
term.write("OpenSecurity Mag/RFID Card Writer. \n")
term.write("Enter Card Data: ")
data = string.gsub(term.read(), "\n", "")
term.setCursor(1,3)
term.write("Enter Card Name: ")
name = string.gsub(term.read(), "\n", "")
term.setCursor(1,4)
term.write("Lock Card? [y/N]")
lock = term.read()
term.setCursor(1,5)

if lock and (lock == "" or lock:sub(1, 1):lower() == "y") then
  doLock = true
else 
  doLock = false
end

writer.write(data, name, doLock)
term.clear()
term.setCursor(1,1)
