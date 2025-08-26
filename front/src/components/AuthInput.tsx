import React from "react";

interface AuthInputProps {
	label: string;
	name: string;
	type?: string;
	value: string;
	placeholder: string;
	onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
	required?: boolean;
}

const AuthInput: React.FC<AuthInputProps> = ({
	label,
	name,
	type = "text",
	value,
	placeholder,
	onChange,
	required = false,
}) => {
	return (
		<div>
			<label htmlFor={name} className="block text-sm font-medium text-gray-700">
				{label}
				<span className="text-red-500">{required === true ? " *" : null}</span>
			</label>
			<input
				id={name}
				name={name}
				type={type}
				value={value}
				onChange={onChange}
				required={required}
				placeholder={placeholder}
				className="mt-1 p-2 w-full px-3 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
			/>
		</div>
	);
};

export default AuthInput;
