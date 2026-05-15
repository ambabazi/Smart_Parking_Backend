import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowRight, Clock } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { createReservation } from '../api/reservationsApi';

export const Booking = () => {
  const [selectedSlots, setSelectedSlots] = useState(1);
  const [duration] = useState(2);
  const pricePerHour = 1200;
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex items-center justify-center p-6 relative overflow-hidden">
      {/* Background */}
      <div className="absolute inset-0 bg-gradient-to-br from-dark via-dark-lighter to-dark" />
      <div 
        className="absolute inset-0 bg-cover bg-center opacity-20"
        style={{ backgroundImage: 'url(https://images.unsplash.com/photo-1506521781263-d8422e82f27a?w=1920)' }}
      />

      {/* Booking Card */}
      <div className="relative z-10 mt-10 mb-6 sm:mt-12 sm:mb-8 w-full max-w-lg bg-dark-card/90 backdrop-blur-xl border border-gray-800 rounded-xl p-5">
        <div className="mb-4">
          <h2 className="text-lg font-bold mb-1">Kigali Heights A2</h2>
          <p className="text-xs text-gray-400">Zone 4 • 24/7 Monitored</p>
        </div>

        <div className="flex items-center justify-between mb-5 pb-4 border-b border-gray-800">
          <div className="text-lg font-bold text-primary">
            {pricePerHour.toLocaleString()} RWF
            <span className="text-xs text-gray-400 font-normal">/hr</span>
          </div>
        </div>

        {/* Number of Slots */}
        <div className="mb-5">
          <label className="block text-[11px] font-medium mb-2">NUMBER OF SLOTS</label>
          <div className="grid grid-cols-5 gap-2">
            {[1, 2, 3, 4, 5].map((num) => (
              <button
                key={num}
                onClick={() => setSelectedSlots(num)}
                className={`py-2 rounded-lg border text-sm font-semibold transition-all ${
                  selectedSlots === num
                    ? 'border-primary bg-primary/10 text-primary'
                    : 'border-gray-700 hover:border-gray-600'
                }`}
              >
                {num}
              </button>
            ))}
          </div>
        </div>

        {/* Duration */}
        <div className="mb-5">
          <label className="block text-[11px] font-medium mb-2">DURATION</label>
          <div className="grid grid-cols-2 gap-3">
            <div className="bg-dark-lighter p-3 rounded-lg">
              <div className="text-xs text-gray-400 mb-1">Arrival Time</div>
              <div className="text-sm font-bold">14:30 Today</div>
            </div>
            <div className="bg-dark-lighter p-3 rounded-lg">
              <div className="text-xs text-gray-400 mb-1">Stay</div>
              <div className="text-sm font-bold">{duration}h</div>
            </div>
          </div>
          
          <div className="mt-2 flex items-center justify-center">
            <Clock className="text-primary" size={16} />
          </div>
        </div>

        {/* Total Estimate */}
        <div className="mb-4 pb-4 border-b border-gray-800">
          <div className="flex items-baseline justify-between">
            <span className="text-xs text-gray-400">Total Estimate</span>
            <div className="flex items-baseline gap-2">
              <span className="text-lg font-bold">{(pricePerHour * duration * selectedSlots).toLocaleString()} RWF</span>
              <button className="text-xs text-gray-400 hover:text-white">+2</button>
            </div>
          </div>
        </div>

        <Button
          variant="primary"
          className="w-full py-2.5 text-sm"
          icon={<ArrowRight size={16} />}
          onClick={async () => {
            const parkingId = searchParams.get('parkingSpaceId') || undefined;
            if (!parkingId) {
              alert('Missing parking space selection');
              return;
            }

            try {
              const now = new Date();
              const end = new Date(now.getTime() + duration * 60 * 60 * 1000);
              await createReservation({ parkingSpaceId: Number(parkingId), slotCount: selectedSlots, startTime: now.toISOString(), endTime: end.toISOString() });
              navigate('/dashboard');
            } catch (err) {
              console.error(err);
              alert(err instanceof Error ? err.message : 'Reservation failed');
            }
          }}
        >
          Confirm Reservation
        </Button>
      </div>
    </div>
  );
};
