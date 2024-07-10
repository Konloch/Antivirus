import { useState, useEffect } from "react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"

export default function Quarantine() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const apiKey = (typeof window !== "undefined") ? new URL(window.location.href).searchParams.get('key') : "";

  useEffect(() => {
    const fetchFiles = async () => {
      setLoading(true);
      try {
        const response = await fetch('/api/quarantine', {
          method: 'POST',
          body: `action=getFiles&key=${apiKey}`,
        });
        const data = await response.json();
        setFiles(data);
      } catch (error) {
        console.error('Error fetching files:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchFiles();

      setInterval(() => {
        fetchFiles();
      }, 1000);

  }, []);


    const handleFileRemove = (id) => {
      fetch('/api/quarantine', {
        method: 'POST',
        body: `action=removeFile&id=${id}&key=${apiKey}`,
      })
      setFiles((prevFiles) => prevFiles.filter((file) => file['id'] !== id))
    }

    const handleReportFalsePositive = (id) => {
      fetch('/api/quarantine', {
        method: 'POST',
        body: `action=reportFalsePositive&id=${id}&key=${apiKey}`,
      })
      // You can also update the UI to reflect the reported false positive
      // For example, you can add a "Reported" badge next to the file name
    }

    const handleRemoveAll = () => {
      fetch('/api/quarantine', {
        method: 'POST',
        body: `action=removeAllFiles&key=${apiKey}`,
      })
      setFiles([])
    }

return (
  <div className="flex flex-col items-center justify-center h-screen bg-background">
    <div className="flex flex-col gap-4">
      {files.length > 0 && (
        <Button onClick={handleRemoveAll}>Remove All</Button>
      )}
      <div className="flex flex-col items-start w-full gap-2 max-h-[200px] overflow-auto">
      {files.length > 0 ? (
        files.map((file) => (
          <div key={file['id']} className="flex items-center space-x-2">
            <p title={file['path']}>{file['name']}</p>
            <Button title="This will whitelist the file and allow it to continue being a part of your OS" variant="outline" onClick={() => handleReportFalsePositive(file['id'])}>
              False Positive
            </Button>
            <Button title="This will delete the file forever" variant="outline" onClick={() => handleFileRemove(file['id'])}>
              Remove Forever
            </Button>
          </div>
        ))
      ) : (
        <p className="text-lg text-gray-500">Quarantine contains no entries</p>
      )}
      </div>
    </div>
  </div>
  )
}