import React, { useState, useEffect } from 'react';
import { QrCode, PlusCircle } from 'lucide-react';
// Assuming you have API functions for these operations
// import { getMyParkingSpaces, registerParkingSlot, generateQrCode } from '../api/parkingApi';
import { Button } from '../components/ui/Button';

// Mock data for now
const mockSpaces = [
  { id: 1, name: 'Kigali Heights Parking', slots: 50, occupied: 20 },
  { id: 2, name: 'CHIC Building Parking', slots: 100, occupied: 95 },
];

const ParkingHub = () => {
  const [spaces, setSpaces] = useState(mockSpaces);
  const [showRegisterSlot, setShowRegisterSlot] = useState(false);

  // TODO: Replace with API call
  // useEffect(() => {
  //   getMyParkingSpaces().then(setSpaces);
  // }, []);

  const handleRegisterSlot = () => {
    // TODO: Implement slot registration logic
    console.log('Registering new slot...');
    setShowRegisterSlot(false);
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-3xl font-bold mb-6">Owner Parking Hub</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {spaces.map(space => (
          <div key={space.id} className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
            <h2 className="text-xl font-semibold mb-2">{space.name}</h2>
            <p className="text-gray-600">Occupancy: {space.occupied} / {space.slots}</p>
            <div className="mt-4 flex justify-between items-center">
              <Button variant="outline">
                <QrCode className="mr-2 h-4 w-4" />
                View QR Codes
              </Button>
              <Button>
                Manage
              </Button>
            </div>
          </div>
        ))}

        <div 
          className="bg-gray-50 p-6 rounded-lg shadow-md border-2 border-dashed border-gray-300 flex flex-col items-center justify-center cursor-pointer hover:bg-gray-100"
          onClick={() => setShowRegisterSlot(true)}
        >
          <PlusCircle className="h-12 w-12 text-gray-400 mb-2" />
          <p className="text-gray-500 font-semibold">Register New Parking Space</p>
        </div>
      </div>

      {showRegisterSlot && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white p-8 rounded-lg shadow-xl max-w-lg w-full">
            <h2 className="text-2xl font-bold mb-4">Register New Parking Space</h2>
            {/* Form to register a new parking slot */}
            <form onSubmit={handleRegisterSlot}>
              {/* Add form fields for name, address, total slots, price etc. */}
              <div className="mb-4">
                <label className="block text-gray-700">Parking Space Name</label>
                <input type="text" className="w-full p-2 border rounded" />
              </div>
              <div className="flex justify-end gap-4">
                <Button variant="ghost" onClick={() => setShowRegisterSlot(false)}>Cancel</Button>
                <Button type="submit">Register</Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ParkingHub;
