package objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharacterHash extends HashSet<Characters> {

	private static final long serialVersionUID = 1659741544544868218L;
	private static final int DEFAULT_CAPACITY = 100;
	private ArrayList<Entry> table;
	private int occupiedSize;

	public CharacterHash() {
		table = new ArrayList<>(DEFAULT_CAPACITY);
		occupiedSize = 0;
		for (int i = 0; i < DEFAULT_CAPACITY; i++) {
			table.add(null);
		}
	}

	// Boolean to inform of a clash
	public boolean put(Characters value) {
		if (value == null)
			return false;

		resize(value);

		int index = hash(value);

		if (index == -1)
			return false;

		while (table.get(index) != null) {
			Entry entry = table.get(index);
			if (entry.key == getkey(value)) {
				// Key already exists, update the value
				entry.value = value;
				return true;
			}
			index++;
		}

		table.set(index, new Entry(getkey(value), value));
		occupiedSize++;

		return false;
	}

	private void resize(Object value) {
		int x = getkey(value);
		if (x >= table.size()) {
			int newSize = x + 20;
			table.ensureCapacity(newSize);

			for (int i = table.size(); i < newSize; i++) {
				table.add(null);
			}
		}

	}

	public List<Entry> getTable() {
		return table;
	}

	@Override
	public boolean equals(Object e) {
		if (e instanceof CharacterHash o) {
			return o.getTable().equals(this.getTable());
		}

		return false;
	}

	@Override
	public int size() {
		return table.size();
	}

	public Characters get(int key) {
		if (key < 0)
			return null;

		int index = key;

		if (index == -1 || index >= table.size())
			return null;

		while (table.get(index) != null) {
			Entry entry = table.get(index);
			if (entry.key == key) {
				return entry.value;
			}
			index++;
		}

		return null;
	}

	public void remove(Characters value) {
		if (value == null)
			return;

		int index = hash(value);

		if (index == -1 || index >= table.size())
			return;

		while (table.get(index) != null) {
			Entry entry = table.get(index);
			if (entry.key == getkey(value)) {
				table.set(index, null);
				occupiedSize--;
				return;
			}
			index++;
		}
	}

	public int getOccupiedSize() {
		return occupiedSize;
	}

	private int hash(Object value) {
		return getkey(value);
	}

	public int getkey(Object value) {
		if (value instanceof Characters characters) {
			return characters.getSiteID();
		}
		return -1;
	}

	private static class Entry implements Serializable {
		private static final long serialVersionUID = -2084321373016689242L;
		int key;
		Characters value;

		Entry(int key, Characters value) {
			this.key = key;
			this.value = value;
		}
	}

	public ArrayList<Characters> toArrayList() {
		ArrayList<Characters> list = new ArrayList<>(getOccupiedSize());

		// Descending as I prefer that in this case
		for (int i = table.size() - 1; i >= 0; i--) {
			if (get(i) != null) {
				list.add(get(i));
			}
		}
		return list;
	}

	@Override
	public boolean isEmpty() {
		return getOccupiedSize() == 0;
	}

	@Override
	public Set<Characters> clone() {
		CharacterHash hashClone = new CharacterHash();
		for (int i = 0; i < this.size(); i++) {
			hashClone.put(this.get(i));
		}
		return hashClone;

	}

}
