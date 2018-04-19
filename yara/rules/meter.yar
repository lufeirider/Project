rule Meterpreter_Shell
{
meta:
author = "lufei"
description = "Meterpreter_Shell"

strings:
	$a = "stdapi_" ascii nocase

condition:
	$a
}
