import { useState, useEffect } from "react"
import { Checkbox } from "@/components/ui/checkbox"
import { Label } from "@/components/ui/label"

export default  function Settings() {
  const [settings, setSettings] = useState({})
  useEffect(() => {
    const fetchSettings = async () => {
      try {
        const response = await fetch("/api/settings/status")
        const data = await response.json()
        setSettings(data)
      } catch (error) {
        console.error("Error fetching settings:", error)
      }
    }
    fetchSettings()
  }, [])
  const handleSettingChange = (setting) => {
    setSettings((prevSettings) => ({
      ...prevSettings,
      [setting]: !prevSettings[setting],
    }))
    fetch("/api/settings/change", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ [setting]: !settings[setting] }),
    })
  }
  return (
    <div className="flex flex-col items-center justify-center h-screen bg-background">
      <div className="flex flex-col gap-4">
        {Object.keys(settings).map((setting) => (
          <div key={setting} className="flex items-center space-x-2">
            <Checkbox
              id={`setting-${setting.toLowerCase()}`}
              checked={settings[setting]}
              onCheckedChange={() => handleSettingChange(setting)}
            />
            <Label htmlFor={`setting-${setting.toLowerCase()}`}>{setting}</Label>
          </div>
        ))}
      </div>
    </div>
  )
}
