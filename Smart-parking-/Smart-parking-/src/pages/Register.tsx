import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Car, Building2, ArrowRight, Grid } from 'lucide-react';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { register as apiRegister } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export const Register = () => {
  const [accountType, setAccountType] = useState<'driver' | 'owner'>('driver');
  const [acceptedTerms, setAcceptedTerms] = useState(false);
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!acceptedTerms) return;

    try {
      await apiRegister({ fullName, email, phone, password, role: accountType === 'owner' ? 'HOST' : 'DRIVER' });
      // Auto-login after successful register
      await login(email, password);
      navigate('/dashboard');
    } catch (err) {
      console.error(err);
      // fallback: navigate to login so user can sign in
      navigate('/login');
    }
  };

  return (
    <div className="min-h-screen grid md:grid-cols-2">
      {/* Left Side - Hero */}
      <div className="relative hidden md:flex items-center justify-center p-12 overflow-hidden">
        <div className="absolute inset-0 bg-cover bg-center" style={{ backgroundImage: "url('/Background%20Image.png')" }} />
        <div className="absolute inset-0 bg-black/60" />
        
        <div className="relative z-10 max-w-md">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-12 h-12 bg-primary rounded-lg flex items-center justify-center">
              <span className="text-dark font-bold text-xl">P</span>
            </div>
            <div>
              <h2 className="text-2xl font-bold text-white">KigaliPark</h2>
            </div>
          </div>

          <h1 className="text-4xl font-bold mb-6">
            Join the future of <br />
            <span className="text-primary">Urban Mobility</span>
          </h1>
          
          <p className="text-gray-300 mb-12">
            Experience the future of urban mobility. Seamlessly find, book, and manage your parking across the city's most premium locations.
          </p>

          <div className="grid grid-cols-2 gap-8">
            <div className="bg-dark-card/50 backdrop-blur-sm p-6 rounded-xl border border-gray-800">
              <Grid className="text-primary mb-3" size={32} />
              <h3 className="font-semibold mb-1">50+</h3>
              <p className="text-sm text-gray-400">Active Hubs</p>
            </div>
            
            <div className="bg-dark-card/50 backdrop-blur-sm p-6 rounded-xl border border-gray-800">
              <div className="text-primary mb-3 text-2xl">⚡</div>
              <h3 className="font-semibold mb-1">Real-time</h3>
              <p className="text-sm text-gray-400">Slot Tracking</p>
            </div>
          </div>
        </div>
      </div>

      {/* Right Side - Register Form */}
      <div className="flex items-center justify-center p-6 md:p-12 bg-dark overflow-y-auto">
        <div className="w-full max-w-md my-2">
          <div className="bg-dark-card/90 border border-gray-800 rounded-[32px] p-3 shadow-2xl">
            <div className="mb-4">
              <h2 className="text-xl font-bold mb-1">Create Account</h2>
              <p className="text-gray-400 text-xs">Select the account type that fits your needs</p>
            </div>

            <div className="grid grid-cols-1 gap-2 mb-4">
              <button
                type="button"
                onClick={() => setAccountType('driver')}
                className={`rounded-3xl border-2 p-2 text-left transition-all ${
                  accountType === 'driver'
                    ? 'border-primary bg-primary/10'
                    : 'border-white/10 bg-white/5 hover:border-primary/30'
                }`}
              >
                <div className="flex items-start gap-2">
                  <div className={`grid h-8 w-8 shrink-0 place-items-center rounded-2xl ${
                    accountType === 'driver' ? 'bg-primary/20' : 'bg-white/10'
                  }`}>
                    <Car className={accountType === 'driver' ? 'text-primary' : 'text-gray-300'} size={16} />
                  </div>
                  <div>
                    <h3 className="text-base font-semibold text-white">Driver</h3>
                    <p className="mt-1 text-xs text-gray-400">Find and book premium parking spots across the city in seconds.</p>
                  </div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => setAccountType('owner')}
                className={`rounded-3xl border-2 p-2 text-left transition-all ${
                  accountType === 'owner'
                    ? 'border-primary bg-primary/10'
                    : 'border-white/10 bg-white/5 hover:border-primary/30'
                }`}
              >
                <div className="flex items-start gap-2">
                  <div className="flex items-start gap-2">
                    <div className={`grid h-8 w-8 shrink-0 place-items-center rounded-2xl ${
                      accountType === 'owner' ? 'bg-primary/20' : 'bg-white/10'
                    }`}>
                      <Building2 className={accountType === 'owner' ? 'text-primary' : 'text-gray-300'} size={20} />
                    </div>
                    <div>
                      <h3 className="text-base font-semibold text-white">Parking Owner</h3>
                      <p className="mt-1 text-xs text-gray-400">Monetize your space and manage availability with professional tools.</p>
                    </div>
                  </div>
                </div>
              </button>
            </div>

            <div className="border-t border-gray-800 pt-6">
              <div className="text-xs uppercase tracking-[0.3em] text-gray-500 mb-4">Account details</div>
              <form onSubmit={handleSubmit} className="space-y-2">
                <Input
                  label="Full Name"
                  type="text"
                  placeholder="Enter your full name"
                  icon={<User size={18} />}
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  required
                />

                <Input
                  label="Work Email"
                  type="email"
                  placeholder="you@company.com"
                  icon={<Mail size={18} />}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />

                <Input
                  label="Phone"
                  type="text"
                  placeholder="+2507XXXXXXXX"
                  icon={<User size={18} />}
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                />

                <Input
                  label="Password"
                  type="password"
                  placeholder="••••••••"
                  icon={<Lock size={18} />}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />

                <div className="flex items-center gap-3 py-2 text-sm text-gray-300">
                  <input
                    type="checkbox"
                    required
                    checked={acceptedTerms}
                    onChange={(e) => setAcceptedTerms(e.target.checked)}
                    aria-label="Agree to Terms and Privacy Policy"
                    className="h-5 w-5 shrink-0 rounded border-gray-700 bg-dark-card accent-primary focus:ring-2 focus:ring-primary/40"
                  />
                  <p className="leading-none">
                    I agree to the{' '}
                    <Link to="/terms" className="font-medium text-primary hover:underline">
                      Terms
                    </Link>{' '}
                    and{' '}
                    <Link to="/privacy" className="font-medium text-primary hover:underline">
                      Privacy Policy
                    </Link>
                    .
                  </p>
                </div>

                <Button
                  type="submit"
                  variant="primary"
                  disabled={!acceptedTerms}
                  className="w-full mt-4"
                  icon={<ArrowRight size={18} />}
                >
                  Create Account
                </Button>
              </form>

              <div className="mt-6 text-center">
                <p className="text-sm text-gray-400">
                  Already have an account?{' '}
                  <Link to="/login" className="text-primary hover:underline">
                    Sign In
                  </Link>
                </p>
              </div>

              <div className="mt-6 pt-4 border-t border-gray-800 text-center">
                <p className="text-xs text-gray-500">
                  By clicking “Create Account”, you agree to KigaliPark’s Terms of Service and Privacy Policy. Securely encrypted by UrbanGuard.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
