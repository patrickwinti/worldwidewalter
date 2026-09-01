/**
 * Deterministic avatar helpers: the same player name always yields the same
 * initials and the same hue, so avatars stay stable across rounds and clients
 * without the backend having to send any avatar data.
 */

export function initials(name: string | undefined | null): string {
  const cleaned = (name ?? '').trim();
  if (cleaned.length === 0) {
    return '?';
  }
  const parts = cleaned.split(/\s+/).filter(Boolean);
  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase();
  }
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

export function avatarHue(name: string | undefined | null): number {
  const cleaned = (name ?? '').trim();
  let hash = 0;
  for (let i = 0; i < cleaned.length; i++) {
    hash = (hash << 5) - hash + cleaned.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash) % 360;
}
