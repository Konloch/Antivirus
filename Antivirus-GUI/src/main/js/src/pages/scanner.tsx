import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Progress } from "@/components/ui/progress"

export default function Scanner() {
  const [scanType, setScanType] = useState(null)
  const [scanProgress, setScanProgress] = useState(0)
  const [scannedFiles, setScannedFiles] = useState([])
  const [scanDuration, setScanDuration] = useState("")
  const [scanRemaining, setScanRemaining] = useState("")
  const [currentFile, setCurrentFile] = useState("")
  const [isScanning, setIsScanning] = useState(false)
  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (isScanning) {
      interval = setInterval(() => {
        fetch("/api/scan/status")
          .then((response) => response.json())
          .then((data) => {
            setScanProgress(data.progress)
            setScannedFiles(data.scannedFiles)
            setCurrentFile(data.currentFile)
            setScanDuration(data.duration)
            setScanRemaining(data.remaining)
          })
      }, 100)
    }
    return () => clearInterval(interval)
  }, [isScanning])
  const handleScan = (type: string) => {
  fetch(`/api/scan/${type}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ scanType: type }),
    }).then(() => {
      setIsScanning(true)
    })
  }
  const handleStopScan = () => {
    fetch("/api/scan/stop", {
      method: "POST",
    }).then(() => {
      setIsScanning(false)
      setScanType(null)
      setScanProgress(0)
      setScannedFiles([])
      setScanDuration("")
      setScanRemaining("")
      setCurrentFile("")
    })
  }
  return (
    <div className="flex flex-col items-center justify-center h-screen bg-background">
      {!isScanning ? (
        <div className="flex flex-col gap-4">
          <Button onClick={() => handleScan("quick")}>Quick Scan</Button>
          <Button onClick={() => handleScan("full")}>Full Scan</Button>
          <Button onClick={() => handleScan("specific")}>Specific File/Folder Scan</Button>
          <div className="text-center"><i>(Drag & drop any file / folder)</i></div>
        </div>
      ) : (
        <div className="flex flex-col items-center gap-4 w-full max-w-md">
          <Progress value={scanProgress} className="w-full" />
          <div className="flex flex-col items-start w-full gap-2 max-h-[200px] overflow-auto">
            {scannedFiles.map((file, index) => (
              <div key={index} className="flex items-center justify-between w-full">
                <span>{file['name']} </span>
                <span
                  className={`px-2 py-1 rounded-full text-xs ${
                    file['status'] === "Quarantined" ? "bg-red-500 text-white" : "bg-green-500 text-white"
                  }`}
                >
                  {file['status']}
                </span>
              </div>
            ))}
          </div>
          <div className="flex items-center justify-between w-full">
            <span>{scanDuration}</span>
            <span>{scanRemaining}</span>
          </div>
          <div className="text-center">{currentFile}</div>
          <Button onClick={handleStopScan}>Stop Scan</Button>
        </div>
      )}
    </div>
  )
}