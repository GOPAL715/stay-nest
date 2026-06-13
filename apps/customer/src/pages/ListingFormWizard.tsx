import { useState } from 'react';
import { LocationPicker, type LocationValue } from '../components/LocationPicker';
import {
  BOOKING_MODES,
  CANCELLATION_POLICIES,
  PROPERTY_TYPES,
  STEPS,
  type ListingFormState,
} from './listingFormShared';

interface ListingFormWizardProps {
  mode: 'create' | 'edit';
  initialForm: ListingFormState;
  onSubmit: (form: ListingFormState) => Promise<void>;
}

export function ListingFormWizard({ mode, initialForm, onSubmit }: ListingFormWizardProps) {
  const [step, setStep] = useState(0);
  const [form, setForm] = useState<ListingFormState>(initialForm);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const update = (patch: Partial<ListingFormState>) => setForm((f) => ({ ...f, ...patch }));

  const setLocation = (location: LocationValue | null) =>
    update({
      latitude: location?.latitude ?? null,
      longitude: location?.longitude ?? null,
    });

  const submit = async () => {
    setSubmitting(true);
    setError(null);
    try {
      await onSubmit(form);
    } catch (err) {
      setError(err instanceof Error ? err.message : `Failed to ${mode} listing`);
    } finally {
      setSubmitting(false);
    }
  };

  const title = mode === 'create' ? 'Create Listing' : 'Edit Listing';
  const submitLabel = mode === 'create' ? 'Create listing' : 'Save changes';
  const submittingLabel = mode === 'create' ? 'Creating…' : 'Saving…';

  return (
    <div className="mx-auto max-w-2xl px-4 py-8 sm:px-6">
      <h2 className="text-3xl font-bold tracking-tight text-gray-900">{title}</h2>
      <p className="mt-2 text-sm text-gray-500">
        Step {step + 1} of {STEPS.length}: {STEPS[step]}
      </p>

      <div className="mt-6 flex gap-2">
        {STEPS.map((label, i) => (
          <div
            key={label}
            className={`h-1 flex-1 rounded ${i <= step ? 'bg-primary-600' : 'bg-gray-200'}`}
          />
        ))}
      </div>

      <div className="card mt-8 space-y-6">
        {step === 0 && (
          <>
            <div>
              <label className="label">Title</label>
              <input className="input" value={form.title} onChange={(e) => update({ title: e.target.value })} />
            </div>
            <div>
              <label className="label">Description</label>
              <textarea
                className="input min-h-[100px]"
                value={form.description}
                onChange={(e) => update({ description: e.target.value })}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Property type</label>
                <select
                  className="input"
                  value={form.propertyType}
                  onChange={(e) => update({ propertyType: e.target.value })}
                >
                  {PROPERTY_TYPES.map((t) => (
                    <option key={t} value={t}>
                      {t}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label">Max guests</label>
                <input
                  type="number"
                  className="input"
                  value={form.maxGuests}
                  onChange={(e) => update({ maxGuests: Number(e.target.value) })}
                />
              </div>
            </div>
          </>
        )}

        {step === 1 && (
          <>
            <div>
              <label className="label">Address</label>
              <input
                className="input"
                value={form.addressLine1}
                onChange={(e) => update({ addressLine1: e.target.value })}
              />
            </div>
            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className="label">City</label>
                <input className="input" value={form.city} onChange={(e) => update({ city: e.target.value })} />
              </div>
              <div>
                <label className="label">State</label>
                <input className="input" value={form.state} onChange={(e) => update({ state: e.target.value })} />
              </div>
              <div>
                <label className="label">Country</label>
                <input
                  className="input"
                  value={form.country}
                  onChange={(e) => update({ country: e.target.value })}
                />
              </div>
            </div>
            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className="label">Bedrooms</label>
                <input
                  type="number"
                  className="input"
                  value={form.bedrooms}
                  onChange={(e) => update({ bedrooms: Number(e.target.value) })}
                />
              </div>
              <div>
                <label className="label">Beds</label>
                <input
                  type="number"
                  className="input"
                  value={form.beds}
                  onChange={(e) => update({ beds: Number(e.target.value) })}
                />
              </div>
              <div>
                <label className="label">Bathrooms</label>
                <input
                  type="number"
                  step="0.5"
                  className="input"
                  value={form.bathrooms}
                  onChange={(e) => update({ bathrooms: Number(e.target.value) })}
                />
              </div>
            </div>
            <LocationPicker
              key="listing-location-map"
              addressLine1={form.addressLine1}
              city={form.city}
              state={form.state}
              country={form.country}
              value={
                form.latitude != null && form.longitude != null
                  ? { latitude: form.latitude, longitude: form.longitude }
                  : null
              }
              onChange={setLocation}
            />
          </>
        )}

        {step === 2 && (
          <>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Nightly rate (paise)</label>
                <input
                  type="number"
                  className="input"
                  value={form.basePricePerNight}
                  onChange={(e) => update({ basePricePerNight: Number(e.target.value) })}
                />
              </div>
              <div>
                <label className="label">Cleaning fee (paise)</label>
                <input
                  type="number"
                  className="input"
                  value={form.cleaningFee}
                  onChange={(e) => update({ cleaningFee: Number(e.target.value) })}
                />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Booking mode</label>
                <select
                  className="input"
                  value={form.bookingMode}
                  onChange={(e) => update({ bookingMode: e.target.value })}
                >
                  {BOOKING_MODES.map((m) => (
                    <option key={m} value={m}>
                      {m.replace('_', ' ')}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label">Cancellation policy</label>
                <select
                  className="input"
                  value={form.cancellationPolicy}
                  onChange={(e) => update({ cancellationPolicy: e.target.value })}
                >
                  {CANCELLATION_POLICIES.map((p) => (
                    <option key={p} value={p}>
                      {p}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </>
        )}

        {step === 3 && (
          <dl className="space-y-2 text-sm">
            <div>
              <dt className="font-medium text-gray-500">Title</dt>
              <dd>{form.title}</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-500">Location</dt>
              <dd>
                {form.city}, {form.state}
              </dd>
            </div>
            <div>
              <dt className="font-medium text-gray-500">Price/night</dt>
              <dd>₹{(form.basePricePerNight / 100).toLocaleString('en-IN')}</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-500">Mode</dt>
              <dd>{form.bookingMode}</dd>
            </div>
          </dl>
        )}

        {error && <p className="text-sm text-red-600">{error}</p>}

        <div className="flex justify-between pt-4">
          <button type="button" className="btn-secondary" disabled={step === 0} onClick={() => setStep((s) => s - 1)}>
            Back
          </button>
          {step < STEPS.length - 1 ? (
            <button type="button" className="btn-primary" onClick={() => setStep((s) => s + 1)}>
              Next
            </button>
          ) : (
            <button type="button" className="btn-primary" disabled={submitting} onClick={() => void submit()}>
              {submitting ? submittingLabel : submitLabel}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
